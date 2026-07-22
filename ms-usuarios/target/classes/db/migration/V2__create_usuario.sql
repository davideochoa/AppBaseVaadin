CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    tipo_usuario_id BIGINT NOT NULL REFERENCES tipo_usuario(id)
);

CREATE INDEX idx_usuario_tipo_usuario_id ON usuario(tipo_usuario_id);
