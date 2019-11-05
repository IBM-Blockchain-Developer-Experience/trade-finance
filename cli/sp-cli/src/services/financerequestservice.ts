import { FinanceRequestService as BaseFinanceRequestService, HttpService } from 'wt-common-cli';

export class FinanceRequestService extends BaseFinanceRequestService {
    constructor(httpService: HttpService) {
        super(httpService);
    }

    async approveFinanceRequest(reqId: string) {
        return this.httpService.put(`financerequests/${reqId}/approve`, {});
    }

    async rejectFinanceRequest(reqId: string) {
        return this.httpService.put(`financerequests/${reqId}/reject`, {});
    }
}
