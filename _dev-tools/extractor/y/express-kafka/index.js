const express = require('express');
const { KafkaClient, Producer, Consumer } = require('kafka-node');

const app = express();
const port = 3000;

// Kafka configuration
const kafkaClient = new KafkaClient({ kafkaHost: 'localhost:9092' });
const producer = new Producer(kafkaClient);



const consumer = new Consumer(
    kafkaClient,
    [{ topic: 'serviceA', partition: 0 }, { topic: 'serviceB', partition: 0 }],
    { autoCommit: true }
);


// Produce a message to Kafka
// http://localhost:3000/serviceA/theMessage
app.get('/:topic/:message', (req, res) => {
    const message = req.params.message;
    const topic = req.params.topic
    const payloads = [{ topic: topic, messages: message }];

    producer.send(payloads, (err, data) => {
        if (err) {
            res.status(500).send('Error producing message: ' + err);
        } else {
            res.send('Message produced: ' + message);
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
    console.log(`Server running at http://localhost:${port}/`);
});
