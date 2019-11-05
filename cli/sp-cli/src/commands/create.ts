import { Command } from './command';
import inquirer = require('inquirer');

export class Create extends Command {
    constructor(dataStore, answers: {[key: string]: any}) {
        super(dataStore, [], answers);
    }

    public async questions() {
        const createAnswers: any = await inquirer.prompt([
            {
                type: 'list',
                name: 'assetType',
                message: 'What would you like to create?',
                choices: ['Go back']
            },
        ]);

        if (createAnswers.assetType === 'Go back') {
            return 'BACK';
        }

        return createAnswers;
    }
}
