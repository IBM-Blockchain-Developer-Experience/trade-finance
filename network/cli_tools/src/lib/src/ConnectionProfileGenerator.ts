import * as fs from 'fs';
import * as path from 'path';
import * as Handlebars from 'handlebars';

export interface IOrg {
    name: string;
    smallName: string;
}

export interface INode {
    name: string;
    url: string;
    port: string;
    org: IOrg;
}

export interface IPeer extends INode {
    eventPort: string;
}

export interface INetwork {
    org: IOrg;
    peers: IPeer[];
    cas: INode[];
    mspDir: string;
    orderer: INode;
}

export class ConnectionProfileGenerator {
    public static generate(networkConfig: INetwork, outputPath: string) {
        const rawTemplate = fs.readFileSync(path.join(__dirname, '../../../fixtures/connection_profile_template.json.tmpl')).toString();

        const configTemplate = Handlebars.compile(rawTemplate);

        fs.writeFileSync(path.resolve(process.cwd(), outputPath), configTemplate(networkConfig));
    }
}