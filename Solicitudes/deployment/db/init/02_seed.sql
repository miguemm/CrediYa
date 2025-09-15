INSERT INTO public.estado (nombre, descripcion) VALUES('Pendiente de revisión', 'Su solicitud esta pendiente de ser revisada por el sistema.') ON CONFLICT DO NOTHING;

INSERT INTO public.estado (nombre, descripcion) VALUES('Aprobada', 'Su solicitud fue aprobada.') ON CONFLICT DO NOTHING;

INSERT INTO public.estado (nombre, descripcion) VALUES('Rechazada', 'Su Solicitud fue rechazada.') ON CONFLICT DO NOTHING;

INSERT INTO public.estado (nombre, descripcion) VALUES('Revision manual', 'Su Solicitud esta pendiente de una revision por parte de un funcionario.') ON CONFLICT DO NOTHING;

INSERT INTO public.tipo_prestamo (nombre, monto_minimo, monto_maximo, tasa_interes, validacion_automatica) VALUES('Crédito libre inversión', 5000000.00, 20000000.00, 10, true) ON CONFLICT DO NOTHING;

INSERT INTO public.tipo_prestamo (nombre, monto_minimo, monto_maximo, tasa_interes, validacion_automatica) VALUES('Crédito libre inversión', 5000000.00, 20000000.00, 15, false) ON CONFLICT DO NOTHING;