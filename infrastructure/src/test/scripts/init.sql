INSERT INTO couriers (id, name, speed, location_x, location_y) VALUES
(gen_random_uuid(), 'Иван Петров', 1, 5, 5),
(uuid('e2f7e7aa-cb1e-455c-9e85-b3e776ba9d12'), 'Анна Сидорова', 2, 2, 8),
(gen_random_uuid(), 'Сергей Васильев', 3, 10, 3);

INSERT INTO orders (id, dest_x, dest_y, volume, status, courier_id) VALUES
(
    gen_random_uuid(),
    1,
    2,
    5,
    'Created',
    (SELECT id FROM couriers WHERE name = 'Иван Петров')
),
(
    uuid('9af55f14-a68b-4947-83f2-84c5c6e584c8'),
    3,
    7,
    2,
    'Assigned',
    (SELECT id FROM couriers WHERE name = 'Анна Сидорова')
),
(
    gen_random_uuid(),
    2,
    5,
    3,
    'Completed',
    (SELECT id FROM couriers WHERE name = 'Сергей Васильев')
);

select * from orders;
select * from couriers;

-- delete from orders