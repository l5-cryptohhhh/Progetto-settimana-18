import Markdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { Link } from "react-router-dom";
import privacy from "../../contenuti/privacy-policy.md?raw";
import cookie from "../../contenuti/cookie-policy.md?raw";
import { useTitolo } from "../formato";

const TESTI = {
  privacy: { titolo: "Privacy Policy", testo: privacy },
  cookie: { titolo: "Cookie Policy", testo: cookie },
};

// I link fra le due pagine nei file .md puntano ai file: qui diventano rotte dell'app.
const ROTTE = { "./privacy-policy.md": "/privacy", "./cookie-policy.md": "/cookie" };

/*
 * react-markdown costruisce elementi React e non interpreta l'HTML grezzo:
 * anche qui nessun HTML iniettato a mano.
 */
const componenti = {
  h1: (p) => <h1 className="text-4xl font-bold tracking-tight md:text-5xl" {...p} />,
  h2: (p) => <h2 className="mt-12 text-2xl font-bold tracking-tight" {...p} />,
  h3: (p) => <h3 className="mt-8 text-lg font-bold tracking-tight" {...p} />,
  p: (p) => <p className="mt-4 leading-relaxed text-testo-2" {...p} />,
  em: (p) => <em className="text-sm not-italic text-testo-2" {...p} />,
  strong: (p) => <strong className="font-semibold text-testo" {...p} />,
  ul: (p) => <ul className="mt-4 flex list-disc flex-col gap-2 pl-5 leading-relaxed text-testo-2 marker:text-accento" {...p} />,
  hr: () => <hr className="my-10 border-linea" />,
  blockquote: (p) => <blockquote className="mt-6 rounded-lg bg-superficie-2 px-5 py-1" {...p} />,
  code: (p) => <code className="cifre rounded-[6px] bg-superficie-2 px-1.5 py-0.5 text-[0.9em] text-testo" {...p} />,
  table: (p) => (
    <div className="mt-6 overflow-x-auto rounded-lg border border-linea">
      <table className="w-full min-w-[560px] text-left text-sm" {...p} />
    </div>
  ),
  thead: (p) => <thead className="bg-superficie-2 text-[13px] text-testo-2" {...p} />,
  th: (p) => <th className="px-4 py-3 font-medium" {...p} />,
  td: (p) => <td className="border-t border-linea px-4 py-3 align-top leading-relaxed text-testo-2" {...p} />,
  a: ({ href = "", children }) =>
    ROTTE[href] ? (
      <Link to={ROTTE[href]} className="font-medium text-testo underline underline-offset-4">{children}</Link>
    ) : (
      <a href={/^https?:|^mailto:/.test(href) ? href : undefined} className="font-medium text-testo underline underline-offset-4" rel="noreferrer noopener" target="_blank">
        {children}
      </a>
    ),
};

export default function Informativa({ tipo }) {
  const { titolo, testo } = TESTI[tipo];
  useTitolo(titolo);
  return (
    <article className="mx-auto max-w-[72ch] px-4 pt-12 md:pt-16">
      <Markdown remarkPlugins={[remarkGfm]} components={componenti}>{testo}</Markdown>
    </article>
  );
}
