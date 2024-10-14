INSERT INTO dbo.users(username, email, password_validation) VALUES
    ('alice', 'alice@gmail.com', '$2a$10$rfB5mueMNJFZlFA1RTZbNOUy48WJn27gK8JInlEIwtjxAB2zaF81q'),
    ('bob', 'bob@gmail.com', '$2a$10$HiAG1gbNntnVCVJlXU.k7OMnkVaO22hIIQLrQBGxesoosntZ4TWW.'),
    ('carol', 'carol@gmail.com', '$2a$10$g46B9qqo3spqc4sMoGDFwuf/cwrjR99od.EDL9C6WD1xfE./.6YSu'),
	('test','test@gmail.com','$2a$10$L1fzpexgfR000oDQjSjI7OTI2fKoY.cvPi2TgFRYBvRnLaBrBNe0a');

INSERT INTO dbo.channels(name, owner_id) VALUES
    ('Channel 1', 1),
    ('Channel 2', 2),
    ('Channel 3', 3);

insert into dbo.join_channels(user_id, ch_id) values
    (2, 3);