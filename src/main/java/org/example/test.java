package org.example;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;
import java.util.concurrent.TimeoutException;

public class test {
    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        //Grab your Hedera Testnet account ID and private key
        AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
        PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));

        AccountId myAccountId2 = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID2"));
        PrivateKey myPrivateKey2 = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY2"));

        Client client = Client.forTestnet();
        client.setOperator(myAccountId, myPrivateKey);
//        System.out.println(client.getMaxAttempts());

        PrivateKey newAccountPrivateKey = PrivateKey.generateED25519();
        PublicKey newAccountPublicKey = newAccountPrivateKey.getPublicKey();
        //Create new account and assign the public key
        TransactionResponse newAccount = new AccountCreateTransaction()
                .setKey(newAccountPublicKey)
                .setInitialBalance(Hbar.fromTinybars(1000))
                .execute(client);
        // Get the new account ID
        AccountId newAccountId = newAccount.getReceipt(client).accountId;
//Log the account ID
        System.out.println("The new account ID is: " + newAccountId);
        //Check the new account's balance
        AccountBalance accountBalance = new AccountBalanceQuery()
                .setAccountId(newAccountId)
                .execute(client);

        System.out.println("The new account balance is: " + accountBalance.hbars);
        //Transfer HBAR
        TransactionResponse sendHbar = new TransferTransaction()
                .addHbarTransfer(myAccountId, Hbar.fromTinybars(-1000)) //Sending account
                .addHbarTransfer(newAccountId, Hbar.fromTinybars(1000)) //Receiving account
                .execute(client);
        System.out.println("The transfer transaction was: " + sendHbar.getReceipt(client).status);
        //Request the cost of the query
        Hbar queryCost = new AccountBalanceQuery()
                .setAccountId(newAccountId)
                .getCost(client);

        System.out.println("The cost of this query is: " + queryCost);
        //Check the new account's balance
        AccountBalance accountBalanceNew = new AccountBalanceQuery()
                .setAccountId(newAccountId)
                .execute(client);

        System.out.println("The new account balance is: " + accountBalanceNew.hbars);
        //Create the account balance query
        AccountBalanceQuery query = new AccountBalanceQuery()
                .setAccountId(myAccountId);

//Sign with client operator private key and submit the query to a Hedera network
        AccountBalance accountBalance1 = query.execute(client);


        //-------------------------Exercice 1 : Consensus Service-----------------//


//Print the balance of hbars
        System.out.println("The hbar account balance for this account is " + accountBalance1.hbars);
        //Create the transaction
        TopicCreateTransaction transaction = new TopicCreateTransaction();

//Sign with the client operator private key and submit the transaction to a Hedera network
        TransactionResponse txResponse = transaction.execute(client);

//Request the receipt of the transaction
        TransactionReceipt receipt = txResponse.getReceipt(client);

//Get the topic ID
        TopicId newTopicId = receipt.topicId;

        System.out.println("The new topic ID is " + newTopicId);

//v2.0.0
        //Create the transaction
        TopicCreateTransaction transaction1 = new TopicCreateTransaction()
                .setAdminKey(myPrivateKey);

//Get the admin key from the transaction
        Key getKey = transaction1.getAdminKey();

//V2.0.0

//v2.0.0
        //Create the transaction
        TopicMessageSubmitTransaction transaction3 = new TopicMessageSubmitTransaction()
                .setTopicId(newTopicId)
                .setMessage("hello exam");

//Sign with the client operator key and submit transaction to a Hedera network, get transaction ID
        TransactionResponse txResponse1 = transaction3.execute(client);

//Request the receipt of the transaction
        TransactionReceipt receipt1 = txResponse.getReceipt(client);

//Get the transaction consensus status
        Status transactionStatus = receipt1.status;

        System.out.println("The transaction consensus status is " + transactionStatus);
//v2.0.0





        //-------------------------Exercice 2 : Token Service-----------------//

        TokenCreateTransaction transaction4 = new TokenCreateTransaction()
                .setTokenName("token")
                .setTokenSymbol("bks")
                .setTreasuryAccountId(myAccountId)

                .setInitialSupply(10)

                .setFreezeKey(myPrivateKey)

                .setAdminKey(myPrivateKey.getPublicKey())
                .setMaxTransactionFee(new Hbar(30))
                .freezeWith(client); //Change the default max transaction fee

//Build the unsigned transaction, sign with admin private key of the token, sign with the token treasury private key, submit the transaction to a Hedera network
        TransactionResponse txResponse3 = transaction4.freezeWith(client).sign(myPrivateKey).sign(myPrivateKey).execute(client);

//Request the receipt of the transaction
        TransactionReceipt receipt3 = txResponse3.getReceipt(client);

//Get the token ID from the receipt
        TokenId tokenId = receipt3.tokenId;

        System.out.println("The new token ID is " + tokenId);

//v2.0.1
        // Max transaction fee as a constant
        final int MAX_TRANSACTION_FEE = 20;

// IPFS content identifiers for which we will create a NFT
        String[] CID = {
                "ipfs:metadata.json",
                "ipfs://bafyreiao6ajgsfji6qsgbqwdtjdu5gmul7tv2v3pd6kjgcw5o65b2ogst4/metadata.json",
                "ipfs://bafyreic463uarchq4mlufp7pvfkfut7zeqsqmn3b2x3jjxwcjqx6b5pk7q/metadata.json",
                "ipfs://bafyreihhja55q6h2rijscl3gra7a3ntiroyglz45z5wlyxdzs6kjh2dinu/metadata.json",
                "ipfs://bafyreidb23oehkttjbff3gdi4vz7mjijcxjyxadwg32pngod4huozcwphu/metadata.json",
                "ipfs://bafyreie7ftl6erd5etz5gscfwfiwjmht3b52cevdrf7hjwxx5ddns7zneu/metadata.json"

        };

//Mint a new NFT
        TokenMintTransaction mintTx = new TokenMintTransaction()
                .setTokenId(tokenId)
                .setMaxTransactionFee(new Hbar(MAX_TRANSACTION_FEE))
                .freezeWith(client);

        for (String cid : CID) {
            mintTx.addMetadata(cid.getBytes());
        }

//Sign transaction with the supply key
        TokenMintTransaction mintTxSign = mintTx.sign(myPrivateKey);

//Submit the transaction to a Hedera network
        TransactionResponse mintTxSubmit = mintTxSign.execute(client);

//Get the transaction receipt
        TransactionReceipt mintRx = mintTxSubmit.getReceipt(client);

//Log the serial number
        System.out.println("Created NFT " + tokenId + " with serial: " + mintRx.serials);

        //Create the associate transaction and sign with Alice's key
        TokenAssociateTransaction associateAliceTx = new TokenAssociateTransaction()
                .setAccountId(myAccountId)
                .setTokenIds((List<TokenId>) tokenId)
                .freezeWith(client)
                .sign(myPrivateKey);

//Submit the transaction to a Hedera network
        TransactionResponse associateTxSubmit = associateAliceTx.execute(client);

//Get the transaction receipt
        TransactionReceipt associateAliceRx = associateTxSubmit.getReceipt(client);

//Confirm the transaction was successful
        System.out.println("NFT association with account: " +associateAliceRx.status);

        // Check the balance before the transfer for the treasury account
        AccountBalance balanceCheckTreasury = new AccountBalanceQuery().setAccountId(myAccountId).execute(client);
        System.out.println("Treasury balance: " +balanceCheckTreasury.tokens + "NFTs of ID " +tokenId);

// Check the balance before the transfer for Alice's account
        AccountBalance balanceCheckAlice = new AccountBalanceQuery().setAccountId(myAccountId2).execute(client);
        System.out.println("Alice's balance: " +balanceCheckAlice.tokens + "NFTs of ID " +tokenId);

// Transfer the NFT from treasury to Alice
// Sign with the treasury key to authorize the transfer
        TransferTransaction tokenTransferTx = new TransferTransaction()
                .addNftTransfer( new NftId(tokenId, 1), myAccountId, myAccountId2)
                .freezeWith(client)
                .sign(myPrivateKey);

        TransactionResponse tokenTransferSubmit = tokenTransferTx.execute(client);
        TransactionReceipt tokenTransferRx = tokenTransferSubmit.getReceipt(client);

        System.out.println("NFT transfer from Treasury to Alice: " +tokenTransferRx.status);

// Check the balance of the treasury account after the transfer
        AccountBalance balanceCheckTreasury2 = new AccountBalanceQuery().setAccountId(myAccountId).execute(client);
        System.out.println("my balance: " +balanceCheckTreasury2.tokens + "NFTs of ID " + tokenId);

// Check the balance of Alice's account after the transfer
        AccountBalance balanceCheckAlice2 = new AccountBalanceQuery().setAccountId(myAccountId2).execute(client);
        System.out.println("account 2 balance: " +balanceCheckAlice2.tokens +  "NFTs of ID " +tokenId);




    }

}
