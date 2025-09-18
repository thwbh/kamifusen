import { TimelineItem } from '../types/timeline';

export const experienceData: TimelineItem[] = [
  {
    id: 'openvalue',
    company: 'OpenValue GmbH',
    url: 'https://openvalue.eu/',
    timeframe: '2021 - Present',
    role: 'Senior Software Engineer',
    stack: 'Java, Apache Kafka, SpringBoot, Postgres, MongoDB, AWS, Kubernetes, Docker, Jenkins',
    description: 'Consultant for governmental institutions and globally distributed fintech companies. Anchor for technical expertise for the development of new features and bug fixes. Agile environment that required me to wear multiple hats at once: Lead Developer, DevOps Engineer, Software Architect and Technical Lead.',
    isActive: true
  },
  {
    id: 'raiffeisen',
    company: 'Raiffeisen Software GmbH',
    url: 'https://r-software.at/',
    timeframe: '04/2018 - 04/2021',
    role: 'Senior Fullstack Developer',
    stack: 'Java, Angular, OpenAPI, DB2, Postgres, Docker, JSF, Jenkins',
    description: 'Agile development and maintenance of the GEBOS core banking system. Modernization of infrastructure to DevOps standards, development of an in-house Angular framework, migration of legacy Java code. Worked on the first working prototype of a new banking system that deploys to the cloud.',
  },
  {
    id: 'vbv',
    company: 'VBV',
    url: 'https://vbv.at/',
    timeframe: '04/2015 - 04/2018',
    role: 'Fullstack Developer',
    stack: 'Java, GWT, Oracle, JavaFX, JSP, Swing',
    description: 'Development, maintenance and modernization of the in-house CRM solution. Adaptations for Java 8 support. Redesign and relaunch of the customer self-service portal MeineVBV including the VBV calculator.',
  },
  {
    id: 'massresponse',
    company: 'Mass Response Service GmbH',
    url: 'https://massresponse.com/',
    timeframe: '04/2013 - 04/2015',
    role: 'Fullstack Developer',
    stack: 'Java, Servlets, JavaScript, MySQL',
    description: 'Development and launch of the first fully automated mobile number porting system while in direct contact with Austria\'s mobile network providers. Contributed to the birth of spusu. Implementation of a fraud detection system used in the Eurovision Song Contest 2014.',
  }
];