import type { AIChatMessage } from "@/types/entities";
import { FilesHelper } from "./FilesHelper";
import { encodingForModel } from "js-tiktoken";

export class AIHelper {
    private static instance: AIHelper;

    private filesHelper: FilesHelper = FilesHelper.getInstance();

    private apiKey: string = '';
    private apiEndpoint: string = '';

    getApiKey() { return this.apiKey; }
    getApiEndpoint() { return this.apiEndpoint; }
    setApiKey(apiKey: string) { this.apiKey = apiKey; }
    setApiEndpoint(apiEndpoint: string) { this.apiEndpoint = apiEndpoint; }


    private constructor() { }

    public static getInstance(): AIHelper {
        if (!this.instance) this.instance = new AIHelper();
        return this.instance;
    }



    async generateContextForCodeDoc(dirPath: string, level: number = 10) {

        const fileTree = await this.filesHelper.listFilesRecursive(dirPath);
        const files = fileTree.structure as unknown as {
            text: string,
            isDir: boolean,
            level: number,
            relativePath: string,
        }[];


        const flattenFileTreeList = await this.filesHelper.flattenFileTree(files);
        // console.log('flattenFileTree', flattenFileTreeList);


        let context = `Here is the codebase text:\n<codebase>\n`;

        context += '<FileTree>\n' + flattenFileTreeList.join('\n') + '\n</FileTree>\n'





        // let start_time = Date.now();
        // fs takes less time
        for (const filePath of flattenFileTreeList) {
            const fileContent = await this.filesHelper.readFile(dirPath + filePath).catch((err: any) => {
                return '';
            });

            if (fileContent === '') continue;
            context += `File: ${filePath}\n<code>\n${fileContent}</code>\n`
        }

        // let total_time_fs = Date.now() - start_time;

        // start_time = Date.now();

        // const fileContent = await this._catFile(dirPath + filePath).catch((err: any) => {
        //     return '';
        // });
        // for (const filePath of flattenFileTreeList) {
        //     const fileContent = await this._catFile(dirPath + filePath).catch((err: any) => {
        //         return '';
        //     });

        //     if (fileContent === '') continue;

        //     context += `File: ${filePath}\n<code>\n${fileContent}\n</code>`
        // }
        // let total_time_cat = Date.now() - start_time;




        context += `\n</codebase>\nHere is the user's query:\n<query>\nmake a documentation for the given code\n</query>`

        this.filesHelper.writeToFile('.' + '/context.txt', context);

        return context;
    }

    getTokenCount(text: string) {
        try {

            // console.log('text');
            // const enc = get_encoding('gpt-4o-mini')
            const enc = encodingForModel('gpt-4o-mini')
            // console.log('enc', enc);
            const tokens = enc.encode(text)
            // console.log('tokens', tokens);
            const tokenCount = tokens.length
            // console.log('tokenCount', tokenCount);
            // enc.free();

            return tokens.length
        } catch (e) {
            return -1
        }
    }


    async generateText(content: string | AIChatMessage[], includeContext = false) {


        let body;
        if (includeContext) {
            body = JSON.stringify({
                messages: content
            })
        } else {
            body = JSON.stringify({
                messages: [{ role: "user", content: content }],
            })
        }

        return await fetch(this.getApiEndpoint(), {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "api-key": this.getApiKey(),
            },
            body,
        }).then(res => res.json()).catch(e => {
            console.error(e);
            return {
                success: false,
                message: e?.message || "Failed to generate text"
            }
        });
    }

}