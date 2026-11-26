package com.dappunq.model.ranking;

public record RankingFiltros(String liga, String equipo, String posicion, int pagina, int limite) {
    public RankingFiltros {
        liga = normalizar(liga, "liga");
        equipo = normalizar(equipo, "equipo");
        posicion = normalizar(posicion, "posicion");
        if (pagina < 1) {
            throw new IllegalArgumentException("La pagina debe ser mayor o igual a 1");
        }
        if (limite < 1 || limite > 100) {
            throw new IllegalArgumentException("El limite debe estar entre 1 y 100");
        }
    }

    private static String normalizar(String valor, String campo) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        if (normalizado.isEmpty()) {
            throw new IllegalArgumentException("El filtro " + campo + " no puede estar vacio");
        }
        if (normalizado.length() > 100) {
            throw new IllegalArgumentException("El filtro " + campo + " debe tener como maximo 100 caracteres");
        }
        if (normalizado.chars().anyMatch(c -> Character.isISOControl(c))) {
            throw new IllegalArgumentException("El filtro " + campo + " no puede contener caracteres de control");
        }
        return normalizado;
    }
}
