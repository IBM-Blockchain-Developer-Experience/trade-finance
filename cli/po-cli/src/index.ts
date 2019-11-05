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
    console.log('Welcome to the Bank Portal CLI');
    const authService = new AuthService();
    const dataStore: DataStore = {
        auth: null
    };
    while (true) {
        const answers: any = await questionBuilder(dataStore);
        if (answers.hasOwnProperty('username')) {
            dataStore.auth = {
                additionalPartyIdentification: answers.username,
                gln: await authService.login(answers.username, '')
            };
        } else {
            await route(dataStore, answers);
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
            message: 'What is your username?',
            choices: ['alice@DigiBankPO', 'bob@MagnetoCorpPO'],
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
run()
    .catch((err) => {
        console.log('Program errored');
        console.log(err);
    });
