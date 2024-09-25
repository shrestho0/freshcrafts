import { EngineConnection } from "@/server/EngineConnection";
import type { EngineCommonResponseDto } from "@/types/dtos";
import type { AIChatMessage } from "@/types/entities";
import { AIChatCommands } from "@/types/enums";
// import { ulid } from "@/utils/ulid";
import { ulid } from "ulid";
import { error, json, type RequestHandler } from "@sveltejs/kit";
import { AIHelper } from "@/server/AIHelper";
// import { AzureOpenAI } from "openai";


let apiData: {
    azureChatApiEndpoint: string;
    azureChatApiKey: string;
} | undefined = undefined;

let messages: AIChatMessage[] = [];


// let openAiClient: AzureOpenAI | undefined = undefined;

let syncedWithEngine = false;// flag

export const PATCH: RequestHandler = async ({ request, locals }) => {
    const { command, data, includeContext } = await request.json() as {
        command: AIChatCommands;
        data: any;
        includeContext: boolean,
    }
    console.warn(JSON.stringify({ command, data }));
    try {
        switch (command) {
            case AIChatCommands.INIT_CHAT:

                if (apiData === undefined) {


                    // get api keys from engine,
                    // create openai client
                    // set openAiClient
                    const x = await EngineConnection.getInstance().getChatApiKeys();
                    if (!x?.success) throw new Error(x?.message ?? "Failed to get chat api keys");

                    console.log(x);

                    apiData = x?.payload ?? undefined;
                    if (!apiData) throw new Error("Failed to get chat api keys");






                    // TODO: Uncomment this block to enable AI chat
                    // const msg = await generateText(`Freshcrafts is a selfhosted nodejs deployment platform. Say hello to ${locals.user?.name} on freshcrafts.`)

                    // if (!msg?.choices[0]?.message?.content) throw new Error("Failed to parse AI response ");

                    // const recievedMsg: AIChatMessage = {
                    //     role: "bot",
                    //     content: msg.choices[0].message.content?.toString()!,
                    //     timestamp: new Date().toISOString(),

                    // }
                    // console.log(recievedMsg);
                    // messages.push(recievedMsg);

                    // First message Bot theke na asholeo chole
                    // so,
                    const augmented: AIChatMessage = {
                        role: "bot",
                        content: `Hello, ${locals.user.name}! Welcome to Freshcrafts! 🎉 If you have any questions or need assistance with your Node.js deployment, feel free to ask. Happy crafting!`,
                        timestamp: new Date().toISOString(),
                    }

                    messages.push(augmented);


                    return json({ success: true, message: "Chat initialized", payload: messages });
                }
            // else fall through
            case AIChatCommands.GET_MESSAGES:
                return json({ success: true, payload: messages });
            case AIChatCommands.SEND_MESSAGE:
                let msg = data;

                if (!msg) throw new Error("Message is empty");


                const userMsg: AIChatMessage = {
                    role: "user",
                    content: msg,
                    timestamp: new Date().toISOString(),
                }
                messages.push(userMsg)



                // // // DUMMY
                // // const d: AIChatMessage = {
                // //     role: "bot",
                // //     content: msg,
                // //     timestamp: new Date().toISOString(),
                // // }



                // messages.push(d);
                if (!apiData || !apiData?.azureChatApiEndpoint || !apiData?.azureChatApiKey)
                    throw new Error("OpenAI client not initialized");

                AIHelper.getInstance().setApiEndpoint(apiData.azureChatApiEndpoint);
                AIHelper.getInstance().setApiKey(apiData.azureChatApiKey);
                msg = await AIHelper.getInstance().generateText(JSON.stringify(messages));

                console.log("AI response", msg);

                if (msg?.error) throw new Error(msg.error.message);
                else if (msg?.success != undefined && msg.success == false) throw new Error(msg?.message ?? "Failed to generate text...");
                else if (!msg?.choices[0]?.message?.content) throw new Error("Failed to parse AI response ");

                const recievedMsg: AIChatMessage = {
                    role: "bot",
                    content: msg.choices[0].message.content?.toString()!,
                    timestamp: new Date().toISOString(),

                }
                console.log(recievedMsg);
                messages.push(recievedMsg);

                return json({ success: true, message: "Message sent", payload: messages });
            case AIChatCommands.CLOSE_CHAT_WITH_SYNC:

                // generate chat name from first message of user's
                let chatName: string | undefined;
                for (const msg of messages) {
                    if (msg.role === "user") {
                        chatName = msg.content.slice(0, 10) + "..."
                        break;
                    }
                }

                if (!chatName) chatName = ulid().slice(0, 10);




                const engineRes = await EngineConnection.getInstance().saveAIChatMessages({ chatName, messages });
                if (!engineRes.success) throw new Error(engineRes.message ?? "Failed to save chat messages");
            // fallthrough on success
            case AIChatCommands.CLOSE_CHAT_WITHOUT_SYNC:
                apiData = undefined;
                messages = [];
                return json({ success: true, message: syncedWithEngine ? "Chat saved and closed" : "Chat closed" });



            // send messages to engine


            default:
                throw new Error("Invalid command");


        }
    } catch (e: any) {
        if (e?.message) {
            console.error(e)
        }
        return json({ success: false, message: e?.message ?? "Unknown error occured" });

    }
};

