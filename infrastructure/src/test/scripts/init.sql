INSERT INTO couriers (id, name, speed, location_x, location_y) VALUES
(uuid('1e3895be-ddd8-47d2-8b5e-83f3a121608d'), 'Иван Петров', 1, 5, 5),
(uuid('e2f7e7aa-cb1e-455c-9e85-b3e776ba9d12'), 'Анна Сидорова', 2, 2, 8),
(uuid('e057cf51-d613-403b-901d-a40af73065ec'), 'Сергей Васильев', 3, 10, 3);

INSERT INTO orders (id, dest_x, dest_y, volume, status, courier_id) VALUES
(
	gen_random_uuid(), 1, 2, 5, 'Created',
	null
),
(
	uuid('9af55f14-a68b-4947-83f2-84c5c6e584c8'), 3, 7, 2, 'Assigned',
    (SELECT id FROM couriers WHERE name = 'Анна Сидорова')
),
(
    gen_random_uuid(), 2, 5, 3, 'Completed',
    null
);

insert into storage_places (id, courier_id, name, total_volume, order_id) VALUES
(gen_random_uuid(), uuid('e2f7e7aa-cb1e-455c-9e85-b3e776ba9d12'), 'Короб', 15, null),
(gen_random_uuid(), uuid('e2f7e7aa-cb1e-455c-9e85-b3e776ba9d12'), 'Сумка', 10, uuid('9af55f14-a68b-4947-83f2-84c5c6e584c8')),
(gen_random_uuid(), uuid('1e3895be-ddd8-47d2-8b5e-83f3a121608d'), 'Сумка', 10, null),
(gen_random_uuid(), uuid('e057cf51-d613-403b-901d-a40af73065ec'), 'Сумка', 10, null);


select * from orders;
select * from couriers;
select * from storage_places;

select c.name, sp.courier_id as sp_c_id , sp.name , o.status , o.volume , o.courier_id as o_c_id
from
	storage_places sp
	inner join couriers c on (c.id = sp.courier_id)
	left join orders o on (o.id = sp.order_id )
where
	sp.order_id is null

select sp.courier_id , sp."name" , sp.total_volume , sp.order_id
from
	storage_places sp



-- delete from orders
update orders set courier_id=null where status = 'Created'
