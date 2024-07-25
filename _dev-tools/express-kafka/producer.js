'use strict';

const { KafkaClient, Producer, Offset, KeyedMessage } = require('kafka-node');
// var Producer = kafka.Producer;
// var KeyedMessage = kafka.KeyedMessage;
// var Client = kafka.KafkaClient;
var client = new KafkaClient({ kafkaHost: 'localhost:9092' });
// var argv = require('optimist').argv;
// var topic = argv.topic || 'topic1';
var topic = 'serviceA';
// var p = argv.p || 0;
// var a = argv.a || 0;
// var p = 0;
// var a = 0;
var producer = new Producer(client, { requireAcks: 1 });

producer.on('ready', function () {
    var message = 'a message';
    var keyedMessage = new KeyedMessage('keyed', 'a keyed message');

    producer.send([{ topic: topic, partition: 0, messages: [message, keyedMessage] }], function (
        err,
        result
    ) {
        // console.log(err || result);
        console.log('Message produced:', message);
        console.log("Result", result)
        console.log("Err", err)
        process.exit();
    });
});

producer.on('error', function (err) {
    console.log('error', err);
});