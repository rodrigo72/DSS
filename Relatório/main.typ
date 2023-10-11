#import "cover.typ": cover
#import "template.typ": *

#show: project

#cover(title: "[Inserir título aqui]", authors: (
  (name: "Rodrigo Monteiro", number: "a100706"), 
  (name: "Diogo Abreu", number: "a100646"), 
  (name: "Luís Figueiredo", number: "a100549")), 
  "Outubro, 2023")

#set page(numbering: "i", number-align: center)
#counter(page).update(1)

#heading(numbering: none, outlined: false)[Resumo]
<\<O resumo tem como objectivo descrever de forma sucinta o trabalho realizado. Deverá conter uma pequena introdução, seguida por uma breve descrição do trabalho realizado e terminando com uma indicação sumária do seu estado final. Não deverá exceder as 400 palavras.>>   

\

*Área de Aplicação*: <\<Identificação da Área de trabalho. Por exemplo: Desenho e arquitectura de Sistemas de Bases de Dados.>>

*Palavras-Chave*: <\<Conjunto de palavras-chave que permitirão referenciar domínios de conhecimento, tecnologias, estratégias, etc., directa ou indirectamente referidos no relatório. Por exemplo: Bases de Dados Relacionais, Gestão de Índices, JAVA, Protocolos de Comunicação.>>

#show outline: it => {
    show heading: set text(size: 18pt)
    it
}

#{
  show outline.entry.where(level: 1): it => {
    v(5pt)
    strong(it)
  }

  outline(
    title: [Índice], 
    indent: true, 
  )
}

#v(-0.4em)
#outline(
  title: none,
  target: figure.where(kind: "attachment"),
  indent: n => 1em,
)

#outline(
  title: [Lista de Figuras],
  target: figure.where(kind: image),
)

#outline(
  title: [Lista de Tabelas],
  target: figure.where(kind: table),
)

// Make the page counter reset to 1
#set page(numbering: "1", number-align: center)
#counter(page).update(1)

= Introdução 

= Objetivos

= Modelo de Domínio

#figure(
  caption: "Modelo de domínio",
  kind: image,
  image("images/modelo_de_dominio_v3.png", width: 100%)
)

= Modelo de Use Cases

== Another one

*Use case*: 
- *Descrição*:
- *Cenários*: 
- *Pré-condição*:
- *Pós-condição*:
- *Fluxo normal*:

== Registo de um cliente

*Use case*: Registo de um cliente
- *Descrição*: 
- *Cenários*: 
- *Pré-condição*:
- *Pós-condição*:
- *Fluxo normal*:

== Check-Up

*Use case*: Check-Up
- *Descrição*: Um cliente pede para que seja efetuado um check-up — um serviço gratuito em que se faz a verificação do veículo e se identificam eventuais intervenções que sejam necessárias.
- *Cenários*: (3) Visita à estação de serviço 1
- *Pré-condição*: O cliente e o seu veículo têm de estar registados no sistema
- *Pós-condição*: True (por enquanto)
- *Fluxo normal*:
  1. A ficha do veículo é atualizada, assinalando a necessidade de check-up.
  2. O sistema calcula uma previsão de quando o serviço irá terminar.
  3. Após a conclusão do serviço, a ficha é atualzada novamente.
- *Fluxo alternativo 1:* [Necessidade de execução de outros serviços] (Passo 3) \ 
  3.1  O sistema calcula a ordem de serviço necessária, e uma previsão da hora em que acaba. \
  3.2  O cliente concorda que os serviços sejam efetuados. \
  3.3  Os serviços são efetuados e a ficha do veículo é atualizada. \
- *Fluxo alternativo 2:* [Cliente quer ser notificado] (Passo 3.2) \
  3.2.1 Os serviços são efetuados e a ficha do veículo é atualizada. \
  3.2.2 Quandos os serviços terminam, o cliente é notificado. \
- *Fluxo alternativo 3:* [Cliente não concorda que os serviços sejam efetuados] (Passo 3.2)\
  3.2.1  Os serviços não são efetuados.

= Conclusões

// #heading(numbering: none)[Referências]
#bibliography("refs.bib", title: "Referências")

// #heading(numbering: none)[Lista de Siglas e Acrónimos]

// / BD: Base de Dados
// / DW: Data Warehouse
// / OLTP: On-Line Analyical Processing

#heading(numbering: none)[Anexos]