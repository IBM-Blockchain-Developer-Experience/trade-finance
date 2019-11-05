import inquirer = require('inquirer');
import { CommandRouter } from './commands/commandrouter';
import { PrettyError, DataStore, AuthService } from 'wt-common-cli';

async function questionBuilder(dataStore: DataStore) {
    if (dataStore.auth === null) {
        return login();
    } else {
        // return Promise.resolve({});
        return highLevelActions();
    }
}

async function run() {
    console.log('Welcome to the Trade Finance Demo Service Provider CLI');
    const dataStore: DataStore = {
        auth: null,
    };
    const authService = new AuthService();
    while (true) {
        try {
            const answers: any = await questionBuilder(dataStore);
            if (answers.hasOwnProperty('username')) {
                dataStore.auth = {
                    additionalPartyIdentification: answers.username,
                    gln: await authService.login(answers.username, '')
                };
            } else {
                await route(dataStore, answers);
            }
        } catch (err) {
            if (err instanceof PrettyError) {
                console.log(err.toTable());
            } else {
                console.log(err);
            }
            return run();
        }
    }
}

async function route(dataStore, answers) {
    if (!dataStore.hasOwnProperty('auth')) {
        return run();
    }

    try {
        await CommandRouter.route(dataStore, answers);
    } catch (err) {
        if (err instanceof PrettyError) {
            console.log(err.toTable());
        } else {
            console.log(err);
        }
        const username = answers.username;
        const password = answers.password;
        answers = await highLevelActions();
        answers.username = username;
        answers.password = password;
        await route(dataStore, answers);
    }
}

async function login() {
    const answers: any = await inquirer.prompt([
        {
            type: 'list',
            name: 'username',
            choices: ['admin@DigiBankSP', 'admin@MagnetoCorpSP', 'admin@HedgematicSP', 'admin@EcoBankSP'],
            message: 'What is your username?',
            validate: (answer) => {
                if (answer.split('@').length !== 2) {
                    return 'Username must be of form <NAME>@<ORG>';
                }

                return true;
            }
        }
    ]);

    return answers;
}

function highLevelActions() {
    return inquirer.prompt([
        {
            type: 'list',
            name: 'highLevel',
            message: 'Which action do you want to do?',
            choices: ['Create', 'Read', 'Other', 'Exit']
        },
    ]);
}
run();
