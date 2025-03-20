insert into dbo.users(username, email, password_validation)
values ('alice', 'alice@gmail.com', '$2a$10$rfB5mueMNJFZlFA1RTZbNOUy48WJn27gK8JInlEIwtjxAB2zaF81q'),
       ('bob', 'bob@gmail.com', '$2a$10$HiAG1gbNntnVCVJlXU.k7OMnkVaO22hIIQLrQBGxesoosntZ4TWW.'),
       ('carol', 'carol@gmail.com', '$2a$10$g46B9qqo3spqc4sMoGDFwuf/cwrjR99od.EDL9C6WD1xfE./.6YSu'),
        ('Test99', 'test@gmail.com', '$2a$10$ldq2/7YoJRNQHawLinf63OVbi8Ae81VDn2.3B6k6i1BEe9LLMhdaS'),
       ('admin','admin@gmail.com','$2a$10$f8fEfZ7iPA3A./v4CmaBWOj08Du591h.cyLtWDlvkYQp6aCq.LkV2'),
       ('random','random1@gmail.com','$2a$10$c9EFJuznYm8DmYFKh0kvt.MYQPB7ulrIfHBoMSPRHGE0zzexFKbbG');


insert into dbo.channels(name, owner_id,type)
values ('Channel 1', 1,0),
       ('Channel 2', 2,0),
       ('Channel 3', 3,0);




