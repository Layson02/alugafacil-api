-- ============================================================================
-- V1__create_initial_schema.sql
-- Estrutura inicial do banco de dados AlugaFácil
-- ============================================================================

CREATE TABLE clientes (
    id BIGINT IDENTITY(1,1) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    documento VARCHAR(20) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(150) NOT NULL,
    CONSTRAINT pk_clientes PRIMARY KEY (id),
    CONSTRAINT uk_clientes_documento UNIQUE (documento),
    CONSTRAINT uk_clientes_email UNIQUE (email)
);

CREATE TABLE itens (
    id BIGINT IDENTITY(1,1) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    descricao VARCHAR(500) NULL,
    categoria VARCHAR(100) NOT NULL,
    quantidade_total INT NOT NULL,
    ativo BIT NOT NULL DEFAULT 1,
    CONSTRAINT pk_itens PRIMARY KEY (id),
    CONSTRAINT ck_itens_quantidade_total CHECK (quantidade_total > 0)
);

CREATE TABLE reservas (
    id BIGINT IDENTITY(1,1) NOT NULL,
    cliente_id BIGINT NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    criado_em DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_reservas PRIMARY KEY (id),
    CONSTRAINT fk_reservas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT ck_reservas_periodo CHECK (data_fim >= data_inicio),
    CONSTRAINT ck_reservas_status CHECK (status IN ('PENDENTE', 'CONFIRMADA', 'RETIRADA', 'DEVOLVIDA', 'CANCELADA'))
);

CREATE TABLE reserva_itens (
    id BIGINT IDENTITY(1,1) NOT NULL,
    reserva_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    CONSTRAINT pk_reserva_itens PRIMARY KEY (id),
    CONSTRAINT fk_reserva_itens_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id),
    CONSTRAINT fk_reserva_itens_item FOREIGN KEY (item_id) REFERENCES itens (id),
    CONSTRAINT ck_reserva_itens_quantidade CHECK (quantidade > 0)
);

CREATE TABLE retiradas (
    id BIGINT IDENTITY(1,1) NOT NULL,
    reserva_id BIGINT NOT NULL,
    data_retirada DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacao VARCHAR(500) NULL,
    CONSTRAINT pk_retiradas PRIMARY KEY (id),
    CONSTRAINT uk_retiradas_reserva UNIQUE (reserva_id),
    CONSTRAINT fk_retiradas_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id)
);

CREATE TABLE devolucoes (
    id BIGINT IDENTITY(1,1) NOT NULL,
    reserva_id BIGINT NOT NULL,
    data_devolucao DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacao VARCHAR(500) NULL,
    CONSTRAINT pk_devolucoes PRIMARY KEY (id),
    CONSTRAINT uk_devolucoes_reserva UNIQUE (reserva_id),
    CONSTRAINT fk_devolucoes_reserva FOREIGN KEY (reserva_id) REFERENCES reservas (id)
);

-- Índices para buscas de clientes, itens e verificação de conflito de reservas por período
CREATE INDEX idx_clientes_nome ON clientes (nome);

CREATE INDEX idx_itens_nome ON itens (nome);
CREATE INDEX idx_itens_categoria_ativo ON itens (categoria, ativo);

CREATE INDEX idx_reservas_cliente_id ON reservas (cliente_id);
CREATE INDEX idx_reservas_periodo_status ON reservas (data_inicio, data_fim, status);

CREATE INDEX idx_reserva_itens_reserva_id ON reserva_itens (reserva_id);
CREATE INDEX idx_reserva_itens_item_id ON reserva_itens (item_id);
