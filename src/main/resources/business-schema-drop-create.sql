drop table if exists account;
drop table if exists transfer_request;

create table account (
  id bigint primary key,
  balance int
);

create table transfer_request (
  id bigint primary key,
  account_id bigint,
  amount int,
  processed boolean
);