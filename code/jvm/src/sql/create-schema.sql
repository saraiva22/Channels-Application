drop schema if exists dbo cascade;


create schema dbo;



create table dbo.Users
(
    id                  int generated always as identity primary key,
    username            varchar(64) unique not null,
    email               varchar(64) unique not null,
    password_validation varchar(256)       not null
);

create table dbo.Tokens
(
    token_validation varchar(256) primary key,
    user_id          int references dbo.Users (id) on delete cascade on update cascade,
    created_at       bigint not null,
    last_used_at     bigint not null
);

create table dbo.Invitation_Register
(
    id       int generated always as identity primary key,
    user_id  int references dbo.Users (id) on delete cascade on update cascade,
    cod_hash varchar(64) unique not null,
    expired  boolean
);

create table dbo.Channels
(
    id       int generated always as identity primary key,
    name     varchar(64) unique not null,
    owner_id int references dbo.Users (id) on delete cascade on update cascade,
    type     int not null check (type in (0, 1))
);

create table dbo.Join_Channels
(
    user_id int references dbo.Users (id),
    ch_id   serial references dbo.Channels (id)
);

create table dbo.Invitation_Channels
(
    cod_hash    varchar(64) primary key,
    privacy     int not null check (privacy in (0, 1)),
    status      int not null check (status in (0, 1, 2)),
    user_id     int references dbo.Users (id),
    private_ch  serial references dbo.Channels (id)
);

create table dbo.Messages
(
    id         int generated always as identity,
    channel_id serial references dbo.Channels (id),
    user_id    int references dbo.Users (id),
    text       VARCHAR(64) not null,
    create_at  bigint      not null,
    primary key(id, channel_id, user_id)
);

/*
create or replace function insert_invite_private_channels()
    returns trigger as
$$
declare
    new_invite_id    int;
    channel_owner_id int;
begin
    select owner_id, type
    into channel_owner_id
    from dbo.Channels
    where id = NEW.id and type = 0;

    if channel_owner_id is null then
        return NEW;
    end if;

    insert into dbo.Invitation_Channels (cod_hash,expired)
    values (md5(random()::text),false)
    returning id into new_invite_id;

    insert into dbo.Invite_Private_Channels (user_id, private_ch, invite_id, privacy)
    values (channel_owner_id, new.id, new_invite_id, 1);

    return new;
end;
$$ language plpgsql;

create trigger trg_insert_invite_private_channels
    after insert
    on dbo.Channels
    for each row
execute function insert_invite_private_channels();
 */

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
