INSERT INTO public.rol (nombre, descripcion)
VALUES( 'asesor', 'Este es un asesor') ON CONFLICT DO NOTHING;

INSERT INTO public.rol (nombre, descripcion)
VALUES( 'administrador', 'Este es un administrador') ON CONFLICT DO NOTHING;

INSERT INTO public.rol (nombre, descripcion)
VALUES( 'cliente', 'Este es un cliente') ON CONFLICT DO NOTHING;


INSERT INTO public.usuario
(nombres, apellidos, fecha_nacimiento, direccion, telefono, correo_electronico, salario_base, rol_id, contrasenia)
VALUES('Miguel Asesor', 'Mosquera', '1990-05-12', 'Cra 10 # 5-21, Popayán, Cauca', '+57 3001234567', 'asesor@gmail.com', 2500000.00, 1, '$2a$10$P8TscSTcE3.3p2gt2R9P1OJTImGAlNrgeyigYLKE5cf05AyT/Hphm');