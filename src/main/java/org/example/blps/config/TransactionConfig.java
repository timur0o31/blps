package org.example.blps.config;

import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.naming.InitialContext;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {
    @Bean
    public PlatformTransactionManager transactionManager() throws Exception{
        UserTransaction userTransaction = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        TransactionManager transactionManager =  (TransactionManager) new InitialContext().lookup("java:/TransactionManager");
        return new JtaTransactionManager(userTransaction, transactionManager);
    }
}
