Liner2.6
========

[![Build Status](https://travis-ci.org/CLARIN-PL/Liner2.svg)](https://travis-ci.org/CLARIN-PL/Liner2) [![Coverage Status](https://coveralls.io/repos/github/CLARIN-PL/Liner2/badge.svg?branch=feature%2Fspatial-dynamic)](https://coveralls.io/github/CLARIN-PL/Liner2?branch=feature%2Fspatial-dynamic) [![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
 
Copyright (C) Wrocław University of Science and Technology (PWr), 2010-2018. 
All rights reserved.

        
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


Contributors
------------
* **Michał Marcińczuk** (2010–present), 
* **Jan Kocoń** (2014–present), 
* Adam Kaczmarek (2014–2015),
* Michał Krautforst (2013-2015), 
* Dominik Piasecki (2013), 
* Maciej Janicki (2011)


Citing
------------

### System architecture and KPWr NER models

* Java 8
* C++ compiler (gcc 3.0 or higher) for CRF++ (https://taku910.github.io/crfpp/)
* set JAVA_HOME variable:
```bash
export JAVA_HOME=/usr/lib/jvm/default-java
```
* install dh-autoreconf:
```bash
sudo apt-get install dh-autoreconf
```
Marcińczuk, Michał; Kocoń, Jan; Oleksy, Marcin.
_Liner2 — a Generic Framework for Named Entity Recognition_
In: Proceedings of the 6th Workshop on Balto-Slavic Natural Language Processing, 
pages 86–91, Valencia, Spain, 4 April 2017. Association for Computational Linguistics 

\[[PDF](https://www.researchgate.net/publication/315789247_Liner2_-_a_Generic_Framework_for_Named_Entity_Recognition)\]

<details><summary>[Bibtex]</summary>
<p>

```
@InProceedings{W17-1413,
  author = 	"Marci{\'{n}}czuk, Micha{\l}
		and Koco{\'{n}}, Jan
		and Oleksy, Marcin",
  title = 	"Liner2 --- a Generic Framework for Named Entity Recognition",
  booktitle = 	"Proceedings of the 6th Workshop on Balto-Slavic Natural Language Processing",
  year = 	"2017",
  publisher = 	"Association for Computational Linguistics",
  pages = 	"86--91",
  location = 	"Valencia, Spain",
  doi = 	"10.18653/v1/W17-1413",
  url = 	"http://aclweb.org/anthology/W17-1413"
}
```
</p>
</details>    

### NKJP NER model

Marcińczuk, Michał; Kocoń, Jan; Gawor, Michał. 
_Recognition of Named Entities for Polish-Comparison of Deep Learning and Conditional Random Fields Approaches_
Ogrodniczuk, Maciej; Kobyliński, Łukasz (Eds.): 
Proceedings of the PolEval 2018 Workshop, pp. 63-73, Institute of Computer Science, 
Polish Academy of Science, Warszawa, 2018.

\[[PDF](https://www.researchgate.net/publication/328429192_Recognition_of_Named_Entities_for_Polish-Comparison_of_Deep_Learning_and_Conditional_Random_Fields_Approaches)\]

<details><summary>[Bibtex]</summary>
<p>

```
@inproceedings{poldeepner2018,
  title     = "Recognition of Named Entities for Polish-Comparison of Deep Learning and Conditional Random Fields Approaches",
  author    = "Marcińczuk, Michał and Kocoń, Jan and Gawor, Michał",
  year      = "2018",
  editor    = "Ogrodniczuk, Maciej and Kobyliński, Łukasz",
  booktitle = "Proceedings of the PolEval 2018 Workshop",
  location  = "Warsaw, Poland",
  pages     = "77--92",
  publisher = "Institute of Computer Science, Polish Academy of Science"
}
```

</p>
</details>

Service in Docker
------------

### Requirements

* Docker
* Docker Compose
* Python3 (for demo script)


### Setup

Build the Docker:
```bash
docker-compose build
```

Run the service:
```bash
docker-compose up
```

Test the service:
```bash
python3 stuff/python/liner2rmq.py "Pani Ala Nowak mieszkw w Zielonej Górze"
```

Expected output:
```xml
[INFO] Temp route: route-ET7DWN
[INFO] Temp input file: /tmp/ez6s96sn
[INFO] Sent msg 'route-ET7DWN /tmp/ez6s96sn' to liner2-input
[INFO] Temp output file: b'/tmp/ez6s96sn-ner.xml'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE chunkList SYSTEM "ccl.dtd">
<chunkList>
 <chunk id="ch1">
  <sentence id="s1">
   <tok>
    <orth>Pani</orth>
    <lex disamb="1"><base>pani</base><ctag>subst:sg:nom:f</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <tok>
    <orth>Ala</orth>
    <lex disamb="1"><base>Ala</base><ctag>subst:sg:nom:f</ctag></lex>
    <ann chan="persname" head="1">1</ann>
    <ann chan="persname_forename" head="1">1</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">0</ann>
    <prop key="persName:lemma">Ala Nowak</prop>
    <prop key="persname_forename:lemma">Ala</prop>
   </tok>
   <tok>
    <orth>Nowak</orth>
    <lex disamb="1"><base>Nowak</base><ctag>subst:sg:nom:m1</ctag></lex>
    <lex disamb="1"><base>nowak</base><ctag>subst:sg:nom:m1</ctag></lex>
    <ann chan="persname">1</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname" head="1">1</ann>
    <ann chan="placename_settlement">0</ann>
    <prop key="persname_surname:lemma">Nowak</prop>
   </tok>
   <tok>
    <orth>mieszkw</orth>
    <lex disamb="1"><base>mieszkw</base><ctag>subst:sg:nom:m1</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <tok>
    <orth>w</orth>
    <lex disamb="1"><base>w</base><ctag>prep:loc:nwok</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <tok>
    <orth>Zielonej</orth>
    <lex disamb="1"><base>zielony</base><ctag>adj:sg:loc:f:pos</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement" head="1">1</ann>
    <prop key="placename_settlement:lemma">Zielonej G</prop>
   </tok>
   <tok>
    <orth>G</orth>
    <lex disamb="1"><base>G</base><ctag>brev:pun</ctag></lex>
    <lex disamb="1"><base>godzina</base><ctag>brev:pun</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">1</ann>
   </tok>
   <ns/>
   <tok>
    <orth>?</orth>
    <lex disamb="1"><base>?</base><ctag>interp</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <ns/>
   <tok>
    <orth>?</orth>
    <lex disamb="1"><base>?</base><ctag>interp</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <ns/>
   <tok>
    <orth>rze</orth>
    <lex disamb="1"><base>rze</base><ctag>subst:sg:nom:n</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">0</ann>
   </tok>
  </sentence>
 </chunk>
</chunkList>
```

------------

### Requirements

#### Compilation

* Java 8
* C++ compiler (gcc 3.0 or higher) for [CRF++](https://taku910.github.io/crfpp/)
* set JAVA_HOME variable:
```bash
export JAVA_HOME=/usr/lib/jvm/default-java
```
* install dh-autoreconf:
```bash
sudo apt-get install dh-autoreconf
```

#### Runtime

* Java 8
* [CRF++ 0.57](https://taku910.github.io/crfpp/)

Optional libraries:

* Polem (https://github.com/CLARIN-PL/Polem) — required by models using Polem to lemmatize phrases.
  * `config-nkjp-poleval2018-polem.ini` from `liner26_model_ner_nkjp` 
* RabbitMQ (https://www.rabbitmq.com) — required to run Liner2 in service mode.
* [WCRFT2](http://nlp.pwr.wroc.pl/redmine/projects/wcrft/wiki) — morphological tagger required for `plain:wcrft` input format.

Installation
------------

### Compile

If you do not have CRF++ installed then do the following steps:
```bash
cd g419-external-dependencies
tar -xvf CRF++-0.57.tar.gz
cd CRF++-0.57
./configure
make
sudo make install
sudo ldconfig
```

Then:

```bash
./gradlew jar
```

### Runtime test


```bash
./liner2-cli
```

Output:

```bash
*-----------------------------------------------------------------------------------------------*
* A framework for multitask sequence labeling, including: named entities, temporal expressions. *
*                                                                                               *
* Authors: Michał Marcińczuk (2010–2016), Jan Kocoń (2014–2016), Adam Kaczmarek (2014–2015)     *
*    Past: Michał Krautforst (2013-2015), Dominik Piasecki (2013), Maciej Janicki (2011)        *
* Contact: michal.marcinczuk@pwr.wroc.pl                                                        *
*                                                                                               *
*          G4.19 Research Group, Wrocław University of Technology                               *
*-----------------------------------------------------------------------------------------------*


Use one of the following tools:
 - agreement           -- checks agreement (of annotations) between suplied documents
 - agreement2          -- compare sets of annotations for each pair of corpora. One set is
                          treated as a reference set and the other as a set to evaluate. It is a
                          refactored version of the agreement action.
 - annotations         -- generates an arff file with a list of annotations and their features
 - constituents-eval   -- evaluates normalizer against a specific set of documents (-i
                          batch:FORMAT, -i FORMAT)
 - convert             -- converts documents from one format to another and applies defined
                          converters
 - curve               -- brak opisu
 - eval                -- evaluates chunkers against a specific set of documents (-i
                          batch:FORMAT, -i FORMAT) #or perform cross validation (-i cv:{format})
 - eval-unique         -- evaluates chunkers against a specific set of documents (-i
                          batch:FORMAT, -i FORMAT) #or perform cross validation (-i
                          cv:{format}). The evaluation is performed on the sets#with unique
                          annotations, i.e. annotations with the same orth/base are treated as a
                          single annotation
 - inplace             -- process documents in place
 - interactive         -- processes text entered directly into the terminal
 - lemmatize           -- ToDo
 - normalizer-eval3    -- processes data with given model
 - normalizer-validate -- Read all annotation and their metadata and look for errors.
 - pipe                -- processes data with given model
 - search              -- earches for a phrases matching given pattern based on a set of token
                          features
 - selection           -- todo
 - stats               -- prints corpus statistics
 - train               -- trains chunkers

usage: ./liner2-cli [action] [options]

```


Pre-trained models
------------------

### KPWr NER for Polish

The package contains three models for recognition named entities according to [KPWr NE guidelines](https://clarin-pl.eu/dspace/handle/11321/294).

* *nam* — named entity boundaries,
* *top9* — coarse-grained categories,
* *n82* — fine-grained categories.

Resources:
* DSpace page: https://clarin-pl.eu/dspace/handle/11321/263 
* Direct link to the package: https://clarin-pl.eu/dspace/bitstream/handle/11321/263/liner25_model_ner_rev1.7z

Download the package:
```bash
cd Liner2
wget -O liner25_model_ner_rev1.7z https://clarin-pl.eu/dspace/bitstream/handle/11321/263/liner25_model_ner_rev1.7z 
```

Unpack the package:
```bash
7z x liner25_model_ner_rev1.7z
```

Process a sample CCL file:
```bash
./liner2-cli pipe -i ccl -o tuples -f stuff/resources/sample-sentence.xml -m liner25_model_ner_rev1/config-top9.ini
```

Expected output:
```bash
(4,11,nam_liv,"Ala Nowak")
(20,28,nam_loc,"Warszawie")
```


### PolEval 2018 Task 2: Named Entity Recognition

Mirror: https://www.dropbox.com/s/wem3fp685zleuq6/liner26_model_ner_nkjp.zip?dl=0

DSpace page: https://clarin-pl.eu/dspace/handle/11321/598 (*temporarily off-line*)

Direct link to the package: https://clarin-pl.eu/dspace/bitstream/handle/11321/598/liner26_model_ner_nkjp.zip (*temporarily off-line*)

Liner2 participated in [PolEval 2018 Task 2 on named entity recognition](http://poleval.pl/results/). 
It got a third place with the following scores:

| Metric  | F1 score |
| ------- |   -----: |
| Final   |    0.810 |
| Exact   |    0.778 |
| Overlap |    0.818 |

Download the package with model:
```bash
cd Liner2
wget -O liner26_model_ner_nkjp.zip https://clarin-pl.eu/dspace/bitstream/handle/11321/598/liner26_model_ner_nkjp.zip 
```

Unpack the model:
```bash
unzip liner26_model_ner_nkjp.zip
```

Process a sample CCL file:
```bash
./liner2-cli pipe -i ccl -o tuples -f stuff/resources/sample-sentence.xml -m liner26_model_ner_nkjp/config-nkjp-poleval2018.ini
```

Expected output:
```bash
(4,6,null,persname_forename,"Ala","Ala")
(4,11,null,persName,"Ala Nowak","Ala Nowak")
(7,11,null,persname_surname,"Nowak","Nowak")
(20,28,null,placename_settlement,"Warszawie","Warszawie")
```


### PolEval 2019 Task 1: Recognition and normalisation of temporal expressions

DSpace page: https://clarin-pl.eu/dspace/handle/11321/668

Download the package with model:
```bash
cd Liner2
wget -0 https://clarin-pl.eu/dspace/bitstream/handle/11321/668/2018-05-15-kpwr-event.tar.gz
```

Unpack the model:
```bash
tar xvzf 2018-05-15-kpwr-event.tar.gz
```

Process a sample CCL file:
```bash
./liner2-cli pipe -m 2018-05-15-kpwr-event/events_model_full/cfg.ini -f 2018-05-15-kpwr-event/test2.xml -i ccl -o tuples
```

Expected output:
```bash
(0,24,null,t3_date,"Ostatnia niedziela września","Ostatnia niedziela września")
(25,34,null,action,"zaskoczyła","zaskoczyła")
(58,67,null,action,"oświetlały","oświetlały")
(99,105,null,action,"unosiło","unosiło")
(127,136,null,action,"wydarzenia","wydarzenia")
(147,155,null,state,"wyglądały","wyglądały")
(156,158,null,modality,"już","już")
(175,186,null,state,"towarzysząca","towarzysząca")
(208,214,null,action,"zdarzeń","zdarzeń")
(215,224,null,aspectual,"rozpoczęła","rozpoczęła")
(225,230,null,action,"awaria","awaria")
(259,270,null,action,"napędzającym","napędzającym")
(309,322,null,action,"unieruchomiony","unieruchomiony")
(361,376,null,action,"unieruchomionych","unieruchomionych")
(384,391,null,action,"dołączył","dołączył")
(392,399,null,state,"należący","należący")
(436,441,null,action,"złamał","złamał")
(452,459,null,action,"Sytuacja","Sytuacja")
(477,485,null,state,"wyglądała","wyglądała")
(516,525,null,state,"należących","należących")
(542,546,null,action,"gasły","gasły")
(569,576,null,action,"pomagała","pomagała")
(582,590,null,action,"regulacja","regulacja")
(595,600,null,action,"zmiana","zmiana")
(610,615,null,action,"groźby","groźby")
(617,622,null,action,"prośby","prośby")
(652,660,null,action,"spotkania","spotkania")
(671,676,null,action,"awaria","awaria")
(718,725,null,action,"diagnozy","diagnozy")
(749,753,null,action,"padło","padło")
(780,788,null,action,"spotkaniu","spotkaniu")
(789,797,null,action,"poprawiło","poprawiło")
(805,814,null,action,"pojawienie","pojawienie")
(831,840,null,action,"przyjechał","przyjechał")
(848,857,null,action,"odzyskanym","odzyskanym")
(896,904,null,action,"przywiózł","przywiózł")
(955,961,null,action,"gościły","gościły")
(990,998,null,action,"wzbudziły","wzbudziły")
(1003,1017,null,action,"zainteresowanie","zainteresowanie")
```
 
Service mode (using RabbitMQ)
============


Introduction
------------

Liner2 can be run as a service which listen to a RabbitMQ queue for upcomming requests (*liner2-input*). 
and submit the results to another queue (*liner2-output*).
The input message (send by the client) should have the following format:
```text
ROUTE_KEY PATH
```
Where:
* ROUTE_KEY — name of a route used to post the results to the output queue. The routing key is used by the client to receive the response for their request ignoring others,
* PATH — an absolute path to the file to process.

For example:
```text
client-001 /tmp/document.txt
```

The message send by the service will contain path to a file which contains the output of processing.


Running the service
-------------------
```bash
./liner2-daemon rabbitmq -m liner26_model_ner_nkjp/config-nkjp-poleval2018.ini -i plain:wcrft
```

Expected output:
```bash
 INFO [Thread-1] (RabbitMqWorker.java:91) - Listing to RabbitMQ on channel liner2-input ...
Consumer amq.ctag-m6D9fIMI_Qsm61BH7HoxlA registered
```

It is possible to run more than one instance of `./liner2-daemon rabbitmq`. However, all of them should use the same model and input format.

Testing
-------

Folder `stuff/python` contains a Python script to test the communication with the service. 
The script takes a text to process, stores the texts in a temporal file, generates a routing key, send both to the `liner2-input` queue and listen to `liner2-output`.
After receiving the response it reads the output file, removes both temporal files and prints the output. 

```bash
python3 stuff/python/liner2rmq.py "Pani Ala Nowak mieszkw w Zielonej Górze"
```

The output should be as follows:
```xml
[INFO] Temp route: route-1DVRP4
[INFO] Temp input file: /tmp/amu7_3at
[INFO] Sent msg 'route-1DVRP4 /tmp/amu7_3at' to liner2-input
[INFO] Temp output file: b'/tmp/amu7_3at-ner.xml'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE chunkList SYSTEM "ccl.dtd">
<chunkList>
 <chunk id="ch1">
  <sentence id="s1">
   <tok>
    <orth>Pani</orth>
    <lex disamb="1"><base>pani</base><ctag>subst:sg:nom:f</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <tok>
    <orth>Ala</orth>
    <lex disamb="1"><base>Ala</base><ctag>subst:sg:nom:f</ctag></lex>
    <ann chan="persname" head="1">1</ann>
    <ann chan="persname_forename" head="1">1</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <tok>
    <orth>Nowak</orth>
    <lex disamb="1"><base>Nowak</base><ctag>subst:sg:nom:m1</ctag></lex>
    <lex disamb="1"><base>nowak</base><ctag>subst:sg:nom:m1</ctag></lex>
    <ann chan="persname">1</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname" head="1">1</ann>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <tok>
    <orth>mieszka</orth>
    <lex disamb="1"><base>mieszkać</base><ctag>fin:sg:ter:imperf</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <tok>
    <orth>w</orth>
    <lex disamb="1"><base>w</base><ctag>prep:loc:nwok</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">0</ann>
   </tok>
   <tok>
    <orth>Zielonej</orth>
    <lex disamb="1"><base>zielony</base><ctag>adj:sg:loc:f:pos</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement" head="1">1</ann>
   </tok>
   <tok>
    <orth>Górze</orth>
    <lex disamb="1"><base>góra</base><ctag>subst:sg:loc:f</ctag></lex>
    <ann chan="persname">0</ann>
    <ann chan="persname_forename">0</ann>
    <ann chan="persname_surname">0</ann>
    <ann chan="placename_settlement">1</ann>
   </tok>
  </sentence>
 </chunk>
</chunkList>
```

Logs on the server side:
```
 INFO [pool-1-thread-5] (RabbitMqWorker.java:99) - Received path: '/tmp/amu7_3at'
 INFO [pool-1-thread-5] (RabbitMqWorker.java:108) - Output saved to /tmp/amu7_3at
 INFO [pool-1-thread-5] (RabbitMqWorker.java:121) - Sent /tmp/amu7_3at-ner.xml to liner2-output:route-1DVRP4'
 INFO [pool-1-thread-5] (RabbitMqWorker.java:84) - Request processing done
```
