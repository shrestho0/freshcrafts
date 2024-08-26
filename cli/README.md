

### Begin setup

- Copy `.env` file from `.env.example`
- `.env` and `setup-env.py` must be in the same directory
- [Checkout all envs](#environment-variables)

**How common .env file is used?**


Each key:value pair is added to service specific env or application.properties files.
eg: `ENGINE_KEY=VALUE` goes to `engine/.../application.properties` as `KEY=VALUE`

Run `python3 freshcrafts.py --setup-env` to setup service specific environments

### Deploy Deployment

#### Install 

Run: `python3 freshcrafts.py --install `

#### Update

(update from github source is completed yet, manullary update files by running `git pull`)

Run: `python3 freshcrafts.py --update `

#### Github App

- there will be some screenshots

### Environment Variables

Details commented on .env/.env.example
