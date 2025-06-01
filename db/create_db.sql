BEGIN;

CREATE TABLE IF NOT EXISTS public.deliveries
(
    id bigserial NOT NULL,
    delivery_date date,
    edge_id bigint NOT NULL,
    expected_date date,
    CONSTRAINT deliveries_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.delivery_products
(
    product_id bigint NOT NULL,
    deliveries_id bigint NOT NULL,
    quantity integer NOT NULL,
    CONSTRAINT delivery_products_pkey PRIMARY KEY (deliveries_id, product_id)
    );

CREATE TABLE IF NOT EXISTS public.products
(
    id bigserial NOT NULL,
    sku character varying(12) COLLATE pg_catalog."default" NOT NULL,
    name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    volume numeric(10, 2) NOT NULL,
    weight numeric(10, 2) NOT NULL,
    price numeric(10, 2) NOT NULL,
    CONSTRAINT products_pkey PRIMARY KEY (id),
    CONSTRAINT sku UNIQUE (sku)
    );

CREATE TABLE IF NOT EXISTS public.settings
(
    id bigserial NOT NULL,
    r integer NOT NULL,
    start_date date NOT NULL,
    cur_date date NOT NULL,
    CONSTRAINT settings_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.shipment_products
(
    shipment_id bigint NOT NULL,
    product_id bigint NOT NULL,
    quantity integer NOT NULL,
    CONSTRAINT shipment_products_pkey PRIMARY KEY (shipment_id, product_id)
    );

CREATE TABLE IF NOT EXISTS public.shipments
(
    id bigserial NOT NULL,
    shipment_date date NOT NULL,
    warehouse_id bigint NOT NULL,
    CONSTRAINT shipments_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.suppliers
(
    id bigserial NOT NULL,
    name character varying(30) COLLATE pg_catalog."default" NOT NULL,
    latitude numeric(9, 6),
    longitude numeric(9, 6),
    CONSTRAINT suppliers_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.suppliers_products
(
    supplier_id bigint NOT NULL,
    product_id bigint NOT NULL,
    price numeric(10, 2) NOT NULL,
    CONSTRAINT suppliers_products_pkey PRIMARY KEY (supplier_id, product_id)
    );

CREATE TABLE IF NOT EXISTS public.supplychain_edges
(
    from_node_id bigint NOT NULL,
    to_node_id bigint NOT NULL,
    length integer NOT NULL,
    id bigserial NOT NULL,
    CONSTRAINT supplychain_edges_pkey PRIMARY KEY (id),
    CONSTRAINT uq_edge_direction UNIQUE (from_node_id, to_node_id)
    );

CREATE TABLE IF NOT EXISTS public.supplychain_nodes
(
    id bigserial NOT NULL,
    node_type character varying(10) COLLATE pg_catalog."default" NOT NULL,
    warehouse_id bigint,
    supplier_id bigint,
    CONSTRAINT supplychain_nodes_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.warehouses
(
    id bigserial NOT NULL,
    name character varying(30) COLLATE pg_catalog."default" NOT NULL,
    capacity integer NOT NULL,
    latitude numeric(9, 6) NOT NULL,
    longitude numeric(9, 6) NOT NULL,
    holding_cost numeric(10, 2),
    CONSTRAINT warehouses_pkey PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS public.warehouses_products
(
    warehouse_id bigint NOT NULL,
    product_id bigint NOT NULL,
    quantity integer NOT NULL,
    stock_date date NOT NULL,
    CONSTRAINT warehouses_products_pkey PRIMARY KEY (warehouse_id, product_id, stock_date)
    );

ALTER TABLE IF EXISTS public.deliveries
    ADD CONSTRAINT edge_id FOREIGN KEY (edge_id)
    REFERENCES public.supplychain_edges (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.delivery_products
    ADD CONSTRAINT deliveries_id FOREIGN KEY (deliveries_id)
    REFERENCES public.deliveries (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.delivery_products
    ADD CONSTRAINT product_id FOREIGN KEY (product_id)
    REFERENCES public.products (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.shipment_products
    ADD CONSTRAINT product_id FOREIGN KEY (product_id)
    REFERENCES public.products (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.shipment_products
    ADD CONSTRAINT shipment_id FOREIGN KEY (shipment_id)
    REFERENCES public.shipments (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.shipments
    ADD CONSTRAINT warehouse_id FOREIGN KEY (warehouse_id)
    REFERENCES public.warehouses (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.suppliers_products
    ADD CONSTRAINT product_id FOREIGN KEY (product_id)
    REFERENCES public.products (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.suppliers_products
    ADD CONSTRAINT supplier_id FOREIGN KEY (supplier_id)
    REFERENCES public.suppliers (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.supplychain_edges
    ADD CONSTRAINT from_node_id FOREIGN KEY (from_node_id)
    REFERENCES public.supplychain_nodes (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.supplychain_edges
    ADD CONSTRAINT to_node_id FOREIGN KEY (to_node_id)
    REFERENCES public.supplychain_nodes (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.supplychain_nodes
    ADD CONSTRAINT supplier_id FOREIGN KEY (supplier_id)
    REFERENCES public.suppliers (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.supplychain_nodes
    ADD CONSTRAINT warehouse_id FOREIGN KEY (warehouse_id)
    REFERENCES public.warehouses (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.warehouses_products
    ADD CONSTRAINT product_id FOREIGN KEY (product_id)
    REFERENCES public.products (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;


ALTER TABLE IF EXISTS public.warehouses_products
    ADD CONSTRAINT warehouse_id FOREIGN KEY (warehouse_id)
    REFERENCES public.warehouses (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE
    NOT VALID;

END;