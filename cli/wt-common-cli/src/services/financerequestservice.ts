import { HttpService } from './httpservice';

export class FinanceRequestService {
    httpService: HttpService;

    constructor(httpService: HttpService) {
        this.httpService = httpService;
    }

    async getFinanceRequests(user?: string) {
        let url = 'financerequests';

        if (user) {
            url += `?behalfOf=${user}`;
        }

        const financeRequests = await this.httpService.get(url);

        return this.sortFinanceRequests(financeRequests);
    }

    async getFinanceRequestsByGroupHash(hash: string) {
        let financeRequests = await this.httpService.get(`financerequests/group/hash/${hash}`);

        financeRequests = financeRequests.sort((a, b) => {
            const aId = a.id.replace('REQ', '');
            const bId = b.id.replace('REQ', '');

            return aId - bId;
        });

        return financeRequests;
    }

    async getFinanceRequest(reqId: string) {
        return this.httpService.get(`financerequests/${reqId}`);
    }

    protected sortFinanceRequests(financeRequests) {
        return financeRequests.sort((a, b) => {
            const aId = a.id.replace('REQ', '');
            const bId = b.id.replace('REQ', '');

            return aId - bId;
        });
    }
}
