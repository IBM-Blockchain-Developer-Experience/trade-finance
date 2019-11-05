import { HttpService } from 'wt-common-cli';

export class ShipmentService {
    httpService: HttpService;

    constructor(httpService: HttpService) {
        this.httpService = httpService;
    }

    async createShipment(purchaseOrderId: string, units: number, senderId: string, receiverId: string) {
        const shipmentBody = {
            purchaseOrderId,
            units,
            senderId,
            receiverId
        };

        const partialShipment = await this.httpService.post('shipments', shipmentBody);

        return this.getShipment(partialShipment.id);
    }

    async getShipment(id: string) {
        return this.httpService.get(`shipments/${id}`);
    }

    async getShipments(user: string) {
        return this.httpService.get(`shipments?behalfOf=${user}`);
    }
}
