import { Command } from './command';
import { HttpService, HttpServiceConfig, AuthService } from 'wt-common-cli';
import { PrettyError, Party, Util } from 'wt-common-cli';
import inquirer = require('inquirer');
import { PurchaseOrderService } from '../services/purchaseorderservice';
import { FinanceRequestService } from '../services/financerequestservice';
import { ShipmentService } from '../services/shipmentservice';

export class Create extends Command {
    authService: AuthService;
    purchaseOrderService: PurchaseOrderService;
    financeRequestService: FinanceRequestService;
    shipmentService: ShipmentService;
    private spList: Array<string> = ['MagnetoCorpSP', 'HedgematicSP', 'EcoBankSP'];

    constructor(dataStore, answers: { [key: string]: any }) {
        super(dataStore, [], answers);
        const httpConfig: HttpServiceConfig = {
            host: 'http://localhost',
            port: process.env.PO_CLI_PORT,
            baseUrl: '/api'
        };
        const httpService = new HttpService(httpConfig);
        this.authService = new AuthService();
        this.purchaseOrderService = new PurchaseOrderService(httpService);
        this.financeRequestService = new FinanceRequestService(httpService);
        this.shipmentService = new ShipmentService(httpService);

        if (process.env.SP_LIST) {
            this.spList = process.env.SP_LIST.split(',').map((item) => item.trim());
        }
    }

    public async questions() {
        const createAnswers: any = await inquirer.prompt([
            {
                type: 'list',
                name: 'assetType',
                message: 'What would you like to create?',
                choices: ['Purchase Orders', 'Finance Requests', 'Go back']
            },
        ]);
        switch (createAnswers.assetType.replace(' ', '')) {
            case 'PurchaseOrders':
                const purchaseOrder = await this.createPurchaseOrderQuestions();
                return Util.manipulate(purchaseOrder, Util.getPurchaseOrderFields());
            case 'FinanceRequests':
                return await this.createFinanceRequestQuestions();
            case 'Shipments':
                return await this.createShipmentQuestions();
            case 'Goback':
                return 'BACK';
        }
    }

    private async createPurchaseOrderQuestions() {
        const createAnswers: any = await inquirer.prompt([
            {
                type: 'text',
                name: 'sellerAdditionalInformation',
                message: 'What is the sellers bank identification?',
                default: 'bob@MagnetoCorpPO',
                validate: (answer) => {
                    if (answer.split('@').length !== 2) {
                        return 'Username must be of form <NAME>@<ORG>';
                    }

                    return true;
                }
            },
            {
                type: 'text',
                name: 'sellerGln',
                message: 'What is the sellers GLN?',
                default: (answers) => {
                    return this.authService.getGln(answers.sellerAdditionalInformation);
                }
            },
            {
                type: 'list',
                name: 'productName',
                message: 'What is the purchase order for?',
                choices: this.purchaseOrderService.getProductNames()

            },
            {
                type: 'text',
                name: 'productGtin',
                message: 'What is the GTIN of the item?',
                default: (answers) => {
                    return this.purchaseOrderService.getProductGtin(answers.productName);
                }
            },
            {
                type: 'text',
                name: 'productName',
                message: 'What is the product called?',
                when: (answers: any) => {
                    return answers.productName === 'Other';
                },
                validate: (answer) => {
                    return !!answer;
                }
            },
            {
                type: 'text',
                name: 'price',
                message: 'How much is each unit?',
                default: 1,
                validate: (answer) => {
                    return !!answer;
                }
            },
            {
                type: 'text',
                name: 'units',
                message: 'How many units?',
                default: 100,
                validate: (answer) => {
                    return !!answer;
                }
            }
        ]);

        return this.createPurchaseOrder(createAnswers);
    }

    private async createPurchaseOrder(createAnswers) {
        const { sellerGln, sellerAdditionalInformation, price, units, productGtin } = createAnswers;
        const seller: Party = {
            gln: Number(sellerGln),
            additionalPartyIdentification: sellerAdditionalInformation
        };

        const buyer = this.dataStore.auth;

        return this.purchaseOrderService.createPurchaseOrder(buyer, seller, price, units, productGtin);
    }

    private async createFinanceRequestQuestions() {
        // let purchaseOrders = await this.httpService.get('purchaseorders', {user: this.dataStore.auth});
        let purchaseOrders = await this.purchaseOrderService.getPurchaseOrders(this.dataStore.auth.gln);

        purchaseOrders = purchaseOrders.filter((po) => {
            if (!po.response) {
                return false;
            }
            return po.response.responseStatusCode === 'ACCEPTED' && po.seller.gln === this.dataStore.auth.gln;
        });

        if (purchaseOrders.length === 0) {
            throw new PrettyError('No purchase orders available');
        }
        const createAnswers: any = await inquirer.prompt([
            {
                type: 'checkbox',
                name: 'financierIds',
                message: 'Who are you requesting finance from? (minimum of 1)',
                choices: this.spList,
                validate: (answers) => {
                    if (answers.length === 0) {
                        return 'Minimum of 1 financier required';
                    }

                    return true;
                }
            },
            {
                type: 'list',
                name: 'purchaseOrderId',
                choices: purchaseOrders.map((po) => po.id),
                message: 'Which purchase order would you like finance for?'
            },
            {
                type: 'text',
                name: 'amount',
                message: 'How much finance would you like?'
            },
            {
                type: 'text',
                name: 'interest',
                message: 'What is your desired interest rate?'
            },
            {
                type: 'text',
                name: 'monthLength',
                message: 'How long do you want to pay the finance back over?'
            }
        ]);

        createAnswers.requester = this.dataStore.auth;

        return this.createFinanceRequest(createAnswers);
    }

    private async createFinanceRequest(createAnswers) {
        const requester = this.dataStore.auth;
        const { financierIds, purchaseOrderId, amount, interest, monthLength } = createAnswers;

        return this.financeRequestService.createFinanceRequest(requester, financierIds, purchaseOrderId, amount, interest, monthLength);
    }

    private async createShipmentQuestions() {
        let purchaseOrders = await this.purchaseOrderService.getPurchaseOrders(this.dataStore.auth.gln);
        purchaseOrders = purchaseOrders.filter((po) => {
            return po.status === 'APPROVED' && po.sellerId === this.dataStore.auth.gln;
        });
        const createAnswers: any = await inquirer.prompt([
            {
                type: 'list',
                name: 'purchaseOrderId',
                message: 'Which purchase order are you shipping?',
                choices: purchaseOrders.map((po) => po.id)
            },
            {
                type: 'text',
                name: 'units',
                message: 'How many units are you shipping?'
            }
        ]);
        const myOrders = purchaseOrders.filter((po) => po.id === createAnswers.purchaseOrderId);
        if (myOrders.length === 0) {
            throw new PrettyError('No purchase orders available');
        }
        createAnswers.receiverId = myOrders[0].buyer.additionalPartyIdentification;

        return this.createShipment(createAnswers);
    }

    private async createShipment(createAnswers) {
        const { purchaseOrderId, units, receiverId } = createAnswers;

        return this.shipmentService.createShipment(purchaseOrderId, units, this.dataStore.auth.additionalPartyIdentification, receiverId);
    }
}
