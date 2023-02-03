CREATE TABLE ACCOUNT
(
    ID        BIGSERIAL,
    USER_NAME VARCHAR(50),
    BALANCE   INT,


    CONSTRAINT PK_ACCOUNT PRIMARY KEY (ID)
);

-------------------------------------------------------
CREATE TABLE MONEY_DEPOSIT_EVENT
(
    ID             BIGSERIAL,
    ACCOUNT_NUMBER INT,
    AMOUNT         INT CHECK (AMOUNT > 99),
    CHECK (AMOUNT < 1000),

    CONSTRAINT PK_MONEY_DEPOSIT_EVENT PRIMARY KEY (ID),
    CONSTRAINT FK_ACCOUNT_NUMBER_MONEY_DEPOSIT_EVENT FOREIGN KEY (ACCOUNT_NUMBER) REFERENCES ACCOUNT (ID)
);

--------------------------------------------------------------
insert into  ACCOUNT(USER_NAME, BALANCE) values ('SOLMAZ', 0),
                                                 ('SANAZ', 0),
                                                 ('SARA', 0),
                                                 ('SAHAR', 0);