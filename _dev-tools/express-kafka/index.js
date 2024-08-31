const express = require('express');
const { KafkaClient, Producer, Consumer } = require('kafka-node');

const app = express();
const port = 3000;

// Kafka configuration
const kafkaClient = new KafkaClient({ kafkaHost: 'localhost:9092' });
const producer = new Producer(kafkaClient);

let producerReady = false;
const predefinedTopics = ['ENGINE', 'WIZARD_MYSQL', 'WIZARD_POSTGRES', 'WIZARD_MONGO', 'DEP_WIZ', 'NOTIFICATION', "kafka-health-indicator"]
producer.on('ready', () => {
    console.log('Producer ready');
    producer.createTopics(predefinedTopics, false, (err, data) => {
        if (err) {
            console.error('Error creating topics:', err);
            process.exit(69)
        } else {
            console.log('Topics created:', data);
        }
    })
    producerReady = true;
});



const consumer = new Consumer(
    kafkaClient,
    predefinedTopics.map((d) => {
        return {
            topic: d,
            partition: 0
        }
    })
)


// Produce a message to Kafka
// http://localhost:3000/serviceA/theMessage

// app.get('/:topic', (req, res) => {
//     res.send(`

//         <form method="post">
//         <textarea name="message"></textarea>
//         <br>
//         <button>Send</button>
//         </form>

//         `)
// })
// const template = `
//         <form method="get">
//         <br>
//         <select name="topic">
//         <option {{NOTIFICATION}} value="NOTIFICATION">NOTIFICATION</option>
//         <option {{ENGINE}} value="ENGINE">ENGINE</option>
//         <option {{WIZARD_MYSQL}} value="WIZARD_MYSQL">WIZARD_MYSQL</option>
//         <option {{WIZARD_POSTGRES}} value="WIZARD_POSTGRES">WIZARD_POSTGRES</option>
//         <option {{WIZARD_MONGO}} value="WIZARD_MONGO">WIZARD_MONGO</option>
//         <option {{DEP_WIZ}} value="DEP_WIZ">DEP_WIZ</option>
//         </select>
//         <br>
//         <textarea rows="10" cols="50" name="message">{{VALUE}}</textarea>
//         <br>
//         <button>Send</button>
//         </form>
// `
const fs = require('fs')

const template = fs.readFileSync(__dirname + '/x.html').toString()

app.get('/', (req, res) => {
    // const { message } = req.body
    // console.log(req.body, req.query)
    const message = req.query["message"]

    // const message = req.params.message;
    const topic = req.query["topic"]
    const payloads = [{ topic: topic, messages: message }];

    let x = template.replace(`{{${topic}}}`, 'selected')
    x = x.replace('{{VALUE}}', message ?? '')



    if (!producerReady) {
        res.status(500).send(x + '<br><br>Producer not ready yet');
        return;
    } else if (!topic) {
        res.send(x)
        return
    }

    producer.send(payloads, (err, data) => {
        if (err) {
            res.status(500).send('Error producing message: ' + err);
        } else {
            res.send(x + '<br><br>Message produced: ' + `<br><pre style="padding:5px">${message}<pre>`);
        }
    });
});

// Consume messages from Kafka
consumer.on('message', (message) => {
    console.log('Consumed message:', message);
});

// Handle Kafka errors
producer.on('error', (err) => {
    console.error('Producer error:', err);
});

consumer.on('error', (err) => {
    if (err instanceof TopicsNotExistError) {
        // create topic 
        // ignore for now
    } else {

        console.error('Consumer error:', err);
    }
});

// Start the Express server
app.listen(port, () => {
    console.log(consumer.client, 'consumer.client')
    console.log(`Server running at http://localhost:${port}/`);
});
