import { Command } from './command';
import { HttpService, HttpServiceConfig } from 'wt-common-cli';
import { PrettyDisplay, PrettyError } from 'wt-common-cli';
import { PurchaseOrderService } from '../services/purchaseorderservice';
import { FinanceRequestService } from '../services/financerequestservice';
import inquirer = require('inquirer');

export class Other extends Command {
    purchaseOrderService: PurchaseOrderService;
    financeRequestService: FinanceRequestService;
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
    }

    public async questions() {
        const otherAnswers: any = await inquirer.prompt([
            {
                type: 'list',
                name: 'action',
                message: 'What would you like to do?',
                choices: ['Manage Purchase Order', 'Manage Finance Request', 'Go back']
            }
        ]);

        const action = otherAnswers.action;

        switch (action) {
            case 'Manage Purchase Order':
                return this.managePurchaseOrder();
            case 'Manage Finance Request':
                return this.manageFinanceRequest();
            case 'Manage Shipment':
                break;
            case 'Go back':
                return 'BACK';
        }
    }

    private async managePurchaseOrder() {
        let purchaseOrders = await this.purchaseOrderService.getPurchaseOrders(this.dataStore.auth.gln);
        if (!purchaseOrders) {
            purchaseOrders = [];
        }
        purchaseOrders = purchaseOrders.filter((po) => {
            return po.seller.gln === this.dataStore.auth.gln;
        });

        purchaseOrders = purchaseOrders.filter((po) => {
            return !po.response && po.seller.gln === this.dataStore.auth.gln;
        });

        if (purchaseOrders.length === 0) {
            throw new PrettyError('No purchase orders to manage');
        }

        PurchaseOrderService.displayPurchaseOrders(purchaseOrders);

        const manageAnswers: any = await inquirer.prompt([
            {
                type: 'list',
                name: 'purchaseOrderId',
                message: 'Which purchase order do you want to manage?',
                choices: purchaseOrders.map((p) => p.id)
            }
        ]);

        const purchaseOrderId = manageAnswers.purchaseOrderId;
        const purchaseOrder = purchaseOrders.filter((p) => p.id === purchaseOrderId)[0];

        const questions = [];

        if (purchaseOrder.seller.gln === this.dataStore.auth.gln) {
            questions.push(
                {
                    type: 'list',
                    name: 'action',
                    message: 'What do you want to do?',
                    choices: ['Approve Purchase Order', 'Close Purchase Order']
                }
            );
        }

        const poActions: any = await inquirer.prompt(questions);

        switch (poActions.action) {
            case 'Approve Purchase Order':
                await this.purchaseOrderService.acceptPurchaseOrder(purchaseOrder.id);
                return {success: 'Purchase Order approved'};
            case 'Close Purchase Order':
                await this.purchaseOrderService.rejectPurchaseOrder(purchaseOrder.id);
                return {success: 'Purchase Order closed'};
        }
        return {warning: 'No action taken'};
    }

    private async manageFinanceRequest() {
        let financeRequests = await this.financeRequestService.getFinanceRequests(this.dataStore.auth.additionalPartyIdentification);

        financeRequests = financeRequests.filter((fr) => {
            return (fr.status === 'PENDING' || fr.status === 'APPROVED' ) && fr.requesterId === this.dataStore.auth.additionalPartyIdentification;
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
                if (financeRequest.requesterId === this.dataStore.auth.additionalPartyIdentification) {
                    // if it is me
                    questions.push({
                        type: 'list',
                        name: 'action',
                        choices: ['Withdraw'],
                        message: 'What would you like to do with the finance request?'
                    });
                }
                break;
            case 'APPROVED':
                if (financeRequest.requesterId === this.dataStore.auth.additionalPartyIdentification) {
                    // if it is me
                    questions.push({
                        type: 'list',
                        name: 'action',
                        choices: ['Accept', 'Withdraw'],
                        message: 'What would you like to do with the finance request?'
                    });
                }
                break;
        }

        const actionAnswers: any = await inquirer.prompt(questions);

        switch (actionAnswers.action) {
            case 'Accept':
                await this.financeRequestService.acceptFinanceRequest(financeRequest.id);
                break;
            case 'Withdraw':
                await this.financeRequestService.withdrawFinanceRequest(financeRequest.id);
                break;
        }
        return this.financeRequestService.getFinanceRequest(financeRequest.id);
    }
}
