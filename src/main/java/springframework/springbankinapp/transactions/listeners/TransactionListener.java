package springframework.springbankinapp.transactions.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;
import springframework.springbankinapp.transactions.events.TransactionTopUpEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionListener {

    private final JmsTemplate jmsTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void sendTransactionMessage(TransactionTopUpEvent event) {
        log.info("Message sent to ActiveMQ - Transaction to: {} account, operation type: {}, amount: {}",
                event.targetAccountNumber(),
                event.transactionType(),
                event.amount());

        jmsTemplate.convertAndSend("transaction.topUp.queue", event);
    }

}
