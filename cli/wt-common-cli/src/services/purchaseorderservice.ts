import { PrettyDisplay } from '../utils/prettydisplay';
import { Util } from '../utils/util';
import { HttpService } from './httpservice';

export class PurchaseOrderService {

    static displayPurchaseOrders(data) {
        data = Util.manipulate(data, Util.getPurchaseOrderFields());
        if (!Array.isArray(data)) {
            data = [data];
        }
        const table = new PrettyDisplay(data);
        table.display();
    }
    defaultProducts: {[key: string]: number};
    httpService: HttpService;

    constructor(httpService: HttpService) {
        this.defaultProducts = {
            laptops: 987654321,
            socks: 109876543,
            beanie_hats: 111098765,
            other: undefined
        };

        this.httpService = httpService;
    }

    getProductNames() {
        return Object.keys(this.defaultProducts);
    }

    getProductGtins() {
        return this.getProductNames().map((key) => this.defaultProducts[key]);
    }

    getProductGtin(productName: string) {
        return this.defaultProducts[productName];
    }
}
