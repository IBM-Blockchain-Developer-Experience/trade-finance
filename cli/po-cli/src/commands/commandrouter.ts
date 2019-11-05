import { Read } from './read';
import { Create } from './create';
import { Other } from './other';
import { Util, PrettyDisplay } from 'wt-common-cli';

export class CommandRouter {

    public static async route(dataStore, answers: {[key: string]: any}) {
        const highLevel: string = answers.highLevel;

        let data;
        let table;
        switch (highLevel) {
            case 'Create':
                const create = new Create(dataStore, answers);
                data = await create.questions();
                break;
            case 'Read':
                const read = new Read(dataStore, answers);
                data = await read.questions();

                if (data === 'BACK') {
                    return;
                }

                const type = Util.getType(data);
                switch (type) {
                    case 'PurchaseOrder':
                        data = Util.manipulate(data, Util.getPurchaseOrderFields());
                        break;
                }
                break;
            case 'Other':
                const other = new Other(dataStore, answers);
                data = await other.questions();
                break;
            case 'Exit':
                process.exit(0);
        }
        if (data !== 'BACK') {
            table = new PrettyDisplay(data);
            table.display();
        }
    }
}
