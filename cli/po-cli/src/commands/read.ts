import {Command} from './command';
import {HttpService, HttpServiceConfig, PrettyError} from 'wt-common-cli';
import inquirer = require('inquirer');
import { ShipmentService } from '../services/shipmentservice';
import { PurchaseOrderService } from '../services/purchaseorderservice';
import { FinanceRequestService } from '../services/financerequestservice';

export class Read extends Command {
    purchaseOrderService: PurchaseOrderService;
    financeRequestService: FinanceRequestService;
    shipmentService: ShipmentService;
    constructor(dataStore, answers: {[key: string]: any}) {
        super(dataStore, [], answers);
        const httpConfig: HttpServiceConfig = {
            host: 'http://localhost',
            port: process.env.PO_CLI_PORT,
            baseUrl: '/api'
        };
        const httpService = new HttpService(httpConfig);
        this.purchaseOrderService = new PurchaseOrderService(httpService);
        this.financeRequestService = new FinanceRequestService(httpService);
        this.shipmentService = new ShipmentService(httpService);
    }

    public async questions() {
        const readAnswers: any = await inquirer.prompt([
            {
                type: 'list',
                name: 'assetType',
                message: 'What would you like to read?',
                choices: ['Purchase Orders', 'Finance Requests', 'Go back']
            }
        ]);

        if (readAnswers.assetType === 'Go back') {
            return 'BACK';
        }

        return this.getData(readAnswers);
    }

    public async getData(readAnswers: {[key: string]: any}) {
        const assetType = readAnswers.assetType.toLowerCase().replace(' ', '');

        switch (assetType) {
            case 'purchaseorders':
                return this.purchaseOrderService.getPurchaseOrders(this.dataStore.auth.gln);
            case 'financerequests':
                return this.financeRequestService.getFinanceRequests(this.dataStore.auth.additionalPartyIdentification);
            case 'shipments':
                return this.shipmentService.getShipments(this.dataStore.auth.additionalPartyIdentification);
            default:
                throw new PrettyError('Invalid asset type ' + assetType);
        }
    }
}
