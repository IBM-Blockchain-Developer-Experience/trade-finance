
export class Util {
    public static async sleep(ms: number): Promise<void> {
        await new Promise((resolve: any) => setTimeout(resolve, ms));
    }

    public static manipulate(obj, fields): {[key: string]: any} | {[key: string]: any}[] {
        if (Array.isArray(obj)) {
            const newArray: any[] = [];
            for (const o of obj) {
                newArray.push(Util.manipulate(o, fields));
            }
            return newArray;
        }
        const newObj: any = {};
        for (const field in fields) {
            const fieldParts = field.split('.');
            let latestPart = obj;
            for (const part of fieldParts) {
                if (!latestPart) {
                    latestPart = fields[field].default;
                    break;
                }
                latestPart = latestPart[part];
                if (latestPart === undefined) {
                    latestPart = fields[field].default;
                    break;
                }
            }
            if (fields[field].as) {
                newObj[fields[field].as] = latestPart;
            } else {
                newObj[field] = latestPart;
            }
        }
        return newObj;
    }

    public static getType(data) {
        if (Array.isArray(data)) {
            data = data[0];
        }
        if (!data) {
            return null;
        }
        const id = data.id;
        if (id.indexOf('PO') !== -1) {
            return 'PurchaseOrder';
        } else if (id.indexOf('REQ') !== -1) {
            return 'FinanceRequest';
        }
        return null;
    }

    public static getPurchaseOrderFields() {
        return {
            'id': {},
            'buyer.gln': {as: 'Buyer GLN'},
            'buyer.additionalPartyIdentification': {as: 'Buyer'},
            'seller.gln': {as: 'Seller GLN'},
            'seller.additionalPartyIdentification': {as: 'Seller'},
            'response.responseStatusCode': {as: 'Status', default: 'PENDING'},
            'orderLineItem.requestedQuantity': {as: 'Quantity'},
            'orderLineItem.netPrice': {as: 'Unit Price'},
            'orderLineItem.transactionalTradeItem.gtin': {as: 'GTIN'},
            'orderIdentification.contentOwner.gln': { as: 'Content Owner'},
            'hash': {as: 'Hash'}
        };
    }
}
