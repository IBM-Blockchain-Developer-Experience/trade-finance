import Table = require('cli-table');
import moment = require('moment');

export class PrettyDisplay extends Table {

    private static getHead(data: {[key: string]: any}[]) {
        return Object.keys(data[0]);
    }

    private static getValues(data: {[key: string]: any}[], truncHash?: boolean) {
        const dataArray: string[][] = [];
        for (const d of data) {
            const values = Object.keys(d).map((key) => {
                if (key.toLowerCase() === 'hash' && truncHash) {
                    d[key] = d[key].substr(0, 10);
                } else if (key === 'completionDate') {
                    const date = moment(d[key], 'ddd MMM D HH:mm:ss ZZ YYYY').format('DD/MM/YY');
                    d[key] = date;
                }
                return d[key];
            });
            dataArray.push(values);
        }
        return dataArray;
    }
    constructor(data: {[key: string]: any}[]) {
        if (!data || data.length === 0) {
            super();
            this.push(['No data']);
        } else {
            if (!Array.isArray(data)) {
                data = [data];
            }
            const colNames = PrettyDisplay.getHead(data);
            super({
                head: colNames
            });
            const lcColNames = colNames.map((cn) => cn.toLowerCase());

            this.push(...this.cleanData(data, lcColNames.indexOf('hash') !== -1));
        }
    }

    public display() {
        console.log(this.toString());
    }

    private cleanData(data: {[key: string]: any}[], truncHash?: boolean) {
        for (const d of data) {
            for (const key in d) {
                if (typeof key !== 'string') {
                    d[key] = JSON.stringify(d[key]);
                }
            }
        }
        return PrettyDisplay.getValues(data, truncHash);
    }
}
