-- Создание таблицы couriers
CREATE TABLE couriers (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    speed INTEGER NOT NULL,
    location_x INTEGER NOT NULL,
    location_y INTEGER NOT NULL
);

-- Создание таблицы orders
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    dest_x INTEGER NOT NULL,
    dest_y INTEGER NOT NULL,
    volume INTEGER NOT NULL,
    status TEXT NOT NULL,
    courier_id UUID
    -- Здесь можно добавить внешний ключ, если предполагается связь с таблицей couriers
    , FOREIGN KEY (courier_id) REFERENCES couriers(id) ON DELETE SET NULL
);

-- Создание таблицы outbox
CREATE TABLE outbox (
    id UUID PRIMARY KEY,
    type TEXT NOT NULL,
    content TEXT NOT NULL,
    occurred_on_utc TIMESTAMPTZ NOT NULL,
    processed_on_utc TIMESTAMPTZ
);

-- Создание таблицы storage_places
CREATE TABLE storage_places (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    total_volume INTEGER NOT NULL,
    order_id UUID,
    courier_id UUID NOT NULL,
    CONSTRAINT FK_storage_places_couriers_courier_id FOREIGN KEY (courier_id)
        REFERENCES couriers(id) ON DELETE CASCADE
);

-- Создание индекса на courier_id в таблице storage_places
CREATE INDEX IX_storage_places_courier_id ON storage_places (courier_id);