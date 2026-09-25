import { Link } from "react-router-dom";
import { motion } from "motion/react";
import { ALIMENTAZIONI, chilometri, prezzo } from "../formato";
import { FotoAuto, Scheletro } from "./ui";

/** Etichetta grigia sotto la scheda, come «Diesel», «2021» nel sito di riferimento. */
export function Etichetta({ children }) {
  return (
    <span className="cifre inline-flex items-center rounded-[4px] bg-superficie-2 px-2 py-1 text-[12px] font-medium text-testo-2">
      {children}
    </span>
  );
}

export function SchedaCatalogo({ auto, indice = 0 }) {
  return (
    <motion.li
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.2 }}
      transition={{ duration: 0.55, delay: (indice % 3) * 0.06, ease: [0.16, 1, 0.3, 1] }}
    >
      <Link
        to={`/auto/${auto.id}`}
        className="group block overflow-hidden rounded-lg bg-superficie shadow-[0_1px_2px_rgb(13_13_13/0.06),0_8px_24px_-12px_rgb(13_13_13/0.18)] transition-[transform,box-shadow] duration-300 ease-molla hover:-translate-y-1 hover:shadow-[0_2px_4px_rgb(13_13_13/0.08),0_18px_36px_-14px_rgb(13_13_13/0.28)]"
      >
        <div className="relative aspect-[16/10] overflow-hidden bg-superficie-2">
          <FotoAuto auto={auto} className="transition-transform duration-700 ease-molla group-hover:scale-[1.04]" />
          {/* La linea rossa di marca che entra sotto la foto al passaggio del mouse. */}
          <span className="absolute inset-x-0 bottom-0 h-[3px] origin-left scale-x-0 bg-rosso transition-transform duration-500 ease-molla group-hover:scale-x-100" />
        </div>
        <div className="flex flex-col gap-3 p-5">
          <h3 className="truncate text-[15px] font-bold">
            {auto.marca} {auto.modello}
          </h3>
          <p className="cifre text-xl font-bold">{prezzo(auto.prezzo)}</p>
          <div className="flex flex-wrap gap-1.5">
            <Etichetta>{ALIMENTAZIONI[auto.alimentazione]}</Etichetta>
            <Etichetta>{auto.anno}</Etichetta>
            <Etichetta>{chilometri(auto.chilometri)}</Etichetta>
          </div>
        </div>
      </Link>
    </motion.li>
  );
}

export function SchedaCatalogoVuota() {
  return (
    <li className="overflow-hidden rounded-lg bg-superficie shadow-[0_1px_2px_rgb(13_13_13/0.06)]">
      <Scheletro className="aspect-[16/10] rounded-none" />
      <div className="flex flex-col gap-3 p-5">
        <Scheletro className="h-4 w-44" />
        <Scheletro className="h-6 w-24" />
        <div className="flex gap-1.5">
          <Scheletro className="h-6 w-14" />
          <Scheletro className="h-6 w-12" />
          <Scheletro className="h-6 w-20" />
        </div>
      </div>
    </li>
  );
}
