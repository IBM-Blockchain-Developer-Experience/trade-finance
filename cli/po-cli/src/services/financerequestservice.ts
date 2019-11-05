import { FinanceRequestService as BaseFinanceRequestService, HttpService, Party } from 'wt-common-cli';

export class FinanceRequestService extends BaseFinanceRequestService {

    constructor(httpService: HttpService) {
        super(httpService);
    }

    async createFinanceRequest(requester: Party, financierIds: string[], purchaseOrderId: string, amount: number, interest: number, monthLength: number) {
        const financeRequestBody = {
            requester,
            financierIds,
            purchaseOrderId,
            amount,
            interest,
            monthLength
        };

        const partialFrg = await this.httpService.post('financerequests', financeRequestBody);

        return this.getFinanceRequestsByGroupHash(partialFrg.hash);
    }

    async withdrawFinanceRequest(reqId: string) {
        return this.httpService.put(`financerequests/${reqId}/withdraw`, {});
    }

    async acceptFinanceRequest(reqId: string) {
        return this.httpService.put(`financerequests/${reqId}/accept`, {});
    }
}
