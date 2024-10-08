INSERT INTO dbo.users(username, email, password_validation) VALUES
    ('alice', 'alice@gmail.com', '$2a$10$rfB5mueMNJFZlFA1RTZbNOUy48WJn27gK8JInlEIwtjxAB2zaF81q'),
    ('bob', 'bob@gmail.com', '$2a$10$HiAG1gbNntnVCVJlXU.k7OMnkVaO22hIIQLrQBGxesoosntZ4TWW.'),
    ('carol', 'carol@gmail.com', '$2a$10$g46B9qqo3spqc4sMoGDFwuf/cwrjR99od.EDL9C6WD1xfE./.6YSu');

insert into dbo.join_channels(user_id, ch_id) values
    (3, 3),
    (2, 3),
    (2, 2);