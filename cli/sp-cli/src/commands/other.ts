import 'colors';
import { AuthService, DataStore, HttpService, HttpServiceConfig, PrettyDisplay, PrettyError } from 'wt-common-cli';
import { Command } from './command';
import inquirer = require('inquirer');
import { PurchaseOrderService } from '../services/purchaseorderservice';
import { FinanceRequestService } from '../services/financerequestservice';

export class Other extends Command {
    authService: AuthService;
    purchaseOrderService: PurchaseOrderService;
    financeRequestService: FinanceRequestService;
    constructor(dataStore: DataStore, answers: {[key: string]: any}) {
        super(dataStore, [], answers);
        const httpConfig: HttpServiceConfig = {
            host: 'http://localhost',
            port: process.env.SP_CLI_PORT,
            baseUrl: '/api'
        };
        const httpService = new HttpService(httpConfig);
        this.purchaseOrderService = new PurchaseOrderService(httpService);
        this.financeRequestService = new FinanceRequestService(httpService);
        this.authService = new AuthService();
    }

    public async questions() {
        const otherAnswers: any = await inquirer.prompt([
            {
                type: 'list',
                name: 'action',
                message: 'What would you like to do?',
                choices: ['Manage Finance Request', 'Go back']
            }
        ]);

        const action = otherAnswers.action;

        switch (action) {
            case 'Manage Finance Request':
                return this.manageFinanceRequest();
            case 'Manage Shipment':
                break;
            case 'Go back':
                return 'BACK';
        }
    }

    async getPurchaseOrderInfo() {
        return await inquirer.prompt([
            {
                type: 'text',
                name: 'buyerId',
                message: 'What is the buyers bank identification?',
                validate: (answer) => {
                    if (answer.split('@').length !== 2) {
                        return 'Username must be of form <NAME>@<ORG>';
                    }

                    return true;
                }
            },
            {
                type: 'text',
                name: 'buyerGln',
                message: 'What is the buyers GLN?',
                default: (answers) => {
                    return this.authService.getGln(answers.buyerId);
                }
            },
            {
                type: 'text',
                name: 'contentOwnerGln',
                message: 'What is the GLN of the content owner?'
            },
            {
                type: 'text',
                name: 'unitPrice',
                message: 'How much is each unit?'
            },
            {
                type: 'text',
                name: 'quantity',
                message: 'How many units?'
            },
            {
                type: 'text',
                name: 'productGtin',
                message: 'What is the GTIN of the order?'
            }
        ]);
    }

    async validatePurchaseOrder(fr) {
        const poAnswers: any = await this.getPurchaseOrderInfo();
        const purchaseOrderId = fr.purchaseOrderId;
        const seller = {gln: this.authService.getGln(fr.requesterId), additionalPartyIdentification: fr.requesterId};
        const buyer = {gln: poAnswers.buyerGln, additionalPartyIdentification: poAnswers.buyerId};
        const quantity = poAnswers.quantity;
        const unitPrice = poAnswers.unitPrice;
        const productGtin = poAnswers.productGtin;
        const contentOwnerGln = poAnswers.contentOwnerGln;

        const resp = await this.purchaseOrderService.verifyPurchaseOrder(purchaseOrderId, contentOwnerGln, buyer, seller, quantity, unitPrice, productGtin);

        if (resp) {
            const table = new PrettyDisplay([{Verified: '✔'.green}]);
            table.display();
            return resp;
        } else {
            const table = new PrettyDisplay([{Verified: '✗'.red}]);
            table.display();
            throw new PrettyError('Purchase order details do not match');
        }
    }

    private async manageFinanceRequest() {
        const username = this.dataStore.auth.additionalPartyIdentification;
        const financierOrg = username.split('@')[1];
        let financeRequests = await this.financeRequestService.getFinanceRequests();

        financeRequests = financeRequests.filter((fr) => {
            return (fr.financierId === financierOrg) && (fr.status === 'PENDING');
        });

        if (financeRequests.length === 0) {
            throw new PrettyError('No finance requests to manage');
        }

        const table = new PrettyDisplay(financeRequests);
        table.display();

        const manageAnswers: any = await inquirer.prompt([
            {
                type: 'list',
                name: 'financeRequestId',
                message: 'Which finance request do you want to manage?',
                choices: financeRequests.map((p) => p.id)
            }
        ]);
        const financeRequestId = manageAnswers.financeRequestId;
        const financeRequest = financeRequests.filter((f) => f.id === financeRequestId)[0];

        /**
         * PENDING,
         * APPROVED,
         * REJECTED,
         * WITHDRAWN,
         * ACCEPTED
         */
        const questions = [];

        switch (financeRequest.status) {
            case 'PENDING':
                if (financeRequest.financierId === financierOrg) {
                    // if it is me
                    questions.push({
                        type: 'list',
                        name: 'action',
                        choices: ['Approve', 'Reject'],
                        message: 'What would you like to do with the finance request?'
                    });
                } else {
                    throw new PrettyError(`You are not the financier of request (${financeRequest.id})`);
                }
                break;
        }

        const actionAnswers: any = await inquirer.prompt(questions);

        switch (actionAnswers.action) {
            case 'Approve':
                await this.validatePurchaseOrder(financeRequest);
                await this.financeRequestService.approveFinanceRequest(financeRequest.id);
                break;
            case 'Reject':
                await this.financeRequestService.rejectFinanceRequest(financeRequest.id);
                break;
        }
        return this.financeRequestService.getFinanceRequest(financeRequest.id);
    }
}
