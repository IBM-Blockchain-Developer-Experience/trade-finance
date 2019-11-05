import { HttpService, PurchaseOrderService as BasePurchaseOrderService, Party } from 'wt-common-cli';

export class PurchaseOrderService extends BasePurchaseOrderService {
    constructor(httpService: HttpService) {
        super(httpService);
    }

    async verifyPurchaseOrder(purchaseOrderId: string, contentOwnerGln: number, buyer: Party, seller: Party, units: number, price: number, productGtin): Promise<boolean> {
        const resp = await this.httpService.post(`purchaseorders/${purchaseOrderId}/verify`, {
            contentOwnerGln,
            buyer,
            seller,
            units,
            price,
            productGtin,
        });

        return resp.toString() === 'true';
    }
}
