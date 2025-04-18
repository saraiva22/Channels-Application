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
    ch_id   int references dbo.Channels (id),
    state   int not null check (state in (0, 1))
);

create table dbo.Invitation_Channels
(
    cod_hash    varchar(64) primary key,
    privacy     int not null check (privacy in (0, 1)),
    status      int not null check (status in (0, 1, 2)),
    inviter_id  int references dbo.Users (id),
    guest_id    int references dbo.Users (id),
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

create or replace function insert_owner_into_join_channels()
    returns trigger as
$$
begin
    insert into dbo.Join_Channels (user_id, ch_id, state)
    values (NEW.owner_id, NEW.id, 0);
    return NEW;
end;
$$ language plpgsql;

create trigger trg_insert_owner_into_join_channels
    after insert
    on dbo.Channels
    for each row
execute function insert_owner_into_join_channels();


create or replace function insert_new_member_into_private_channel()
    returns trigger as
$$
begin
    insert into dbo.Join_Channels (user_id, ch_id, state)
    values (NEW.guest_id, NEW.private_ch, 0);
    return NEW;
end;
$$ language plpgsql;

create trigger trg_insert_new_member_into_private_channel
    after update
    on dbo.Invitation_Channels
    for each row
    when (OLD.status = 0 and NEW.status = 1)
execute function insert_new_member_into_private_channel();


create or replace function delete_previous_invite_to_private_channel()
    returns trigger as
$$
begin
    delete from dbo.Invitation_Channels
    where guest_id = OLD.user_id and private_ch = OLD.ch_id;
    return OLD;
end;
$$ language plpgsql;

create trigger trg_delete_previous_invite_to_private_channel
    after delete
    on dbo.Join_Channels
    for each row
execute function delete_previous_invite_to_private_channel();
