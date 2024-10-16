create schema dbo;

create table dbo.Users
(
    id                  int generated always as identity primary key,
    username            VARCHAR(64) unique not null,
    email               VARCHAR(64) unique not null,
    password_validation VARCHAR(256)       not null
);

create table dbo.Tokens
(
    token_validation VARCHAR(256) primary key,
    user_id          int references dbo.Users (id) on delete cascade on update cascade,
    created_at       bigint not null,
    last_used_at     bigint not null
);

create table dbo.Invitation_Register
(
    id       int generated always as identity primary key,
    user_id  int references dbo.Users (id) on delete cascade on update cascade,
    cod_hash VARCHAR(64) unique not null,
--    created_at bigint not null,
    expired  boolean
);

create table dbo.Channels
(
    id       int generated always as identity primary key,
    name     VARCHAR(64) unique not null,
    owner_id int references dbo.Users (id) on delete cascade on update cascade
);

create table dbo.Public_Channels
(
    id         int generated always as identity,
    channel_id serial references dbo.Channels (id) on delete cascade on update cascade,
    primary key (id, channel_id)
);

create table dbo.Join_Channels
(
    id      int generated always as identity primary key,
    user_id int references dbo.Users (id),
    ch_id   serial references dbo.Channels (id)
);

create table dbo.Private_Channels
(
    id         int generated always as identity,
    channel_id serial unique references dbo.Channels (id),
    primary key (id, channel_id)
);

create table dbo.Invitation_Channels
(
    id       int generated always as identity primary key,
    cod_hash VARCHAR(64) unique not null
--    created_at bigint not null,
--    expired bigint not null
);

create table dbo.Invite_Private_Channels
(
    id         int generated always as identity primary key,
    user_id    int references dbo.Users (id),
    private_ch serial references dbo.Private_Channels (channel_id),
    invite_id  serial references dbo.Invitation_Channels (id),
    privacy    int not null
);

create table dbo.Messages
(
    id         int generated always as identity primary key,
    channel_id serial references dbo.Channels (id),
    user_id    int references dbo.Users (id),
    text       VARCHAR(64) not null,
    create_at  bigint      not null
);

create or replace function insert_invite_private_channels()
    returns trigger as
$$
declare
    new_invite_id    int;
    channel_owner_id int;
begin
    select owner_id
    into channel_owner_id
    from dbo.Channels
    where id = NEW.channel_id;

    insert into dbo.Invitation_Channels (cod_hash)
    values (md5(random()::text))
    returning id into new_invite_id;

    insert into dbo.Invite_Private_Channels (user_id, private_ch, invite_id, privacy)
    values (channel_owner_id, new.channel_id, new_invite_id, 1);

    return new;
end;
$$ language plpgsql;

create trigger trg_insert_invite_private_channels
    after insert
    on dbo.Private_Channels
    for each row
execute function insert_invite_private_channels();

create or replace function insert_owner_into_join_channels()
    returns trigger as
$$
begin
    insert into dbo.Join_Channels (user_id, ch_id)
    values (NEW.owner_id, NEW.id);
    return NEW;
end;
$$ language plpgsql;

create trigger trg_insert_owner_into_join_channels
    after insert
    on dbo.Channels
    for each row
execute function insert_owner_into_join_channels();