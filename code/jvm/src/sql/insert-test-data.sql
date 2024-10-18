insert into dbo.users(username, email, password_validation)
values ('alice', 'alice@gmail.com', '$2a$10$rfB5mueMNJFZlFA1RTZbNOUy48WJn27gK8JInlEIwtjxAB2zaF81q'),
       ('bob', 'bob@gmail.com', '$2a$10$HiAG1gbNntnVCVJlXU.k7OMnkVaO22hIIQLrQBGxesoosntZ4TWW.'),
       ('carol', 'carol@gmail.com', '$2a$10$g46B9qqo3spqc4sMoGDFwuf/cwrjR99od.EDL9C6WD1xfE./.6YSu'),
       ('test', 'test@gmail.com', '$2a$10$L1fzpexgfR000oDQjSjI7OTI2fKoY.cvPi2TgFRYBvRnLaBrBNe0a');

insert into dbo.channels(name, owner_id)
values ('Channel 1', 1),
       ('Channel 2', 2),
       ('Channel 3', 3);

insert into dbo.public_channels(channel_id)
values (1),
       (2),
       (3);

insert into dbo.join_channels(user_id, ch_id)
values (2, 3);

insert into dbo.messages(channel_id, user_id, text, create_at)
values (3,4,'HELLO WORD!',1729106384)




