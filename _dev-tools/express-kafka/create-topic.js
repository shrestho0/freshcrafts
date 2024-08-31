
const { Producer, KafkaClient } = require('kafka-node');

var client = new KafkaClient({ kafkaHost: 'localhost:9092' });

const producer = new Producer(client, {
    requireAcks: 1,
    ackTimeoutMs: 100
});

const predefinedTopics = ['ENGINE', 'WIZARD_MYSQL', 'WIZARD_POSTGRES', 'WIZARD_MONGO', 'DEP_WIZ', "kafka-health-indicator"]


producer.on('ready', () => {
    console.log('Producer ready');
    producer.createTopics(predefinedTopics, false, (err, data) => {
        if (err) {
            console.error('Error creating topics:', err);
        } else {
            console.log('Topics created:', data);
        }
        process.exit();
    });

});