create schema dbo;

create table dbo.Users(
    id int generated always as identity primary key,
    username VARCHAR(64) unique not null,
    email VARCHAR(64) unique not null,
    password_validation VARCHAR(256) not null
);

create table dbo.Tokens(
    token_validation VARCHAR(256) primary key,
    user_id int references dbo.Users(id),
    created_at bigint not null,
    last_used_at bigint not null
);

create table dbo.Invitation_Register(
    id serial not null primary key,
    user_id int references dbo.Users(id),
    cod_hash VARCHAR(64) unique not null,
    expired BOOLEAN not null
);

create table dbo.Channels(
    id serial not null unique primary key,
    name VARCHAR(64) unique not null,
    owner_id int references dbo.Users(id),
    rules VARCHAR(64) not null
);

create table dbo.Public_Channels(
    channel_id serial unique references dbo.Channels(id)
);

create table dbo.Join_Channels(
    user_id int references dbo.Users(id),
    ch_id serial references dbo.Channels(id)
);

create table dbo.Private_Channels(
    channel_id serial unique references dbo.Channels(id)
);

create table dbo.Invitation_Channels(
    id serial not null unique primary key,
    owner_id int references dbo.Users(id),
    privacy int not null,
    expired BOOLEAN not null
);

create table dbo.Invite_Private_Channels(
    user_id int references dbo.Users(id),
    private_ch serial references dbo.Private_Channels(channel_id),
    invite_id serial references dbo.Invitation_Channels(id)
);

create table dbo.Messages(
    id serial not null unique primary key,
    channel_id serial references dbo.Channels(id),
    user_id int references dbo.Users(id),
    text VARCHAR(64) not null,
    timestamp bigint not null
);