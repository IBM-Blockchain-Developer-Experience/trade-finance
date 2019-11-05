import { PurchaseOrderService as BasePurchaseOrderService, HttpService, Party } from 'wt-common-cli';

export class PurchaseOrderService extends BasePurchaseOrderService {
    constructor(httpService: HttpService) {
        super(httpService);
    }

    async createPurchaseOrder(buyer: Party, seller: Party, price: number, units: number, productGtin: number) {

        const purchaseOrderBody = {
            buyer,
            seller,
            price,
            units,
            productGtin
        };

        const partialOrder = await this.httpService.post('purchaseorders', purchaseOrderBody);

        return this.getPurchaseOrder(partialOrder.id);
    }

    async getPurchaseOrders(gln: number) {
        const purchaseOrders = await this.httpService.get(`purchaseorders?behalfOf=${gln}`);

        const purchaseOrderResponses = await this.getPurchaseOrderResponses(gln);

        for (const po of purchaseOrders) {
            for (const por of purchaseOrderResponses) {
                if (po.id === por.originalOrder.entityIdentification) {
                    po.response = por;
                    break;
                }
            }
        }

        return this.sortPurchaseOrders(purchaseOrders);
    }

    async getPurchaseOrder(poId: string) {
        return this.httpService.get(`purchaseorders/${poId}`);
    }

    async getPurchaseOrderResponse(poId: number) {
        return this.httpService.get(`purchaseorders/${poId}/responses`);
    }

    async getPurchaseOrderResponses(gln: number) {
        return this.httpService.get(`purchaseorders/responses?behalfOf=${gln}`);
    }

    async acceptPurchaseOrder(id: string) {
        return this.httpService.put(`purchaseorders/${id}/accept`, {});
    }

    async rejectPurchaseOrder(id: string) {
        return this.httpService.put(`purchaseorders/${id}/close`, {});
    }

    protected sortPurchaseOrders(purchaseOrders) {
        return purchaseOrders.sort((a, b) => {
            const aId = a.id.replace('PO', '');
            const bId = b.id.replace('PO', '');

            return aId - bId;
        });
    }
}
