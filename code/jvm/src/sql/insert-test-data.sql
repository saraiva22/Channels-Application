insert into dbo.users(username, email, password_validation)
values ('alice', 'alice@gmail.com', '$2a$10$rfB5mueMNJFZlFA1RTZbNOUy48WJn27gK8JInlEIwtjxAB2zaF81q'),
       ('bob', 'bob@gmail.com', '$2a$10$HiAG1gbNntnVCVJlXU.k7OMnkVaO22hIIQLrQBGxesoosntZ4TWW.'),
       ('carol', 'carol@gmail.com', '$2a$10$g46B9qqo3spqc4sMoGDFwuf/cwrjR99od.EDL9C6WD1xfE./.6YSu'),
        ('test', 'test@gmail.com', '$2a$10$L1fzpexgfR000oDQjSjI7OTI2fKoY.cvPi2TgFRYBvRnLaBrBNe0a'),
       ('admin','admin@gmail.com','$2a$10$f8fEfZ7iPA3A./v4CmaBWOj08Du591h.cyLtWDlvkYQp6aCq.LkV2'),
       ('random','random1@gmail.com','$2a$10$f8fEfZ7iPA3A./v4CmaBWOj08Du591h.cyLtWDlvkYQp6aCq.LkV2');


insert into dbo.channels(name, owner_id,type)
values ('Channel 1', 1,0),
       ('Channel 2', 2,0),
       ('Channel 3', 3,0);




