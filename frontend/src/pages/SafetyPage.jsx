import {
  Shield,
  Flag,
  Users,
} from "lucide-react";

function SafetyPage() {
  return (
    <main className="page-shell">
      <section className="page-card wide-card">
        <p className="badge">Safety center</p>

        <h1>Stay safe while using Matchly</h1>

        <p className="page-description">
          Matchly is designed around respectful
          conversations, moderation, reporting, and
          user privacy.
        </p>

        <div className="safety-grid">
          <article>
            <div className="safety-icon">
              <Shield aria-hidden="true" />
            </div>

            <h2>Protect your privacy</h2>

            <p>
              Do not share passwords, addresses, or
              private information.
            </p>
          </article>

          <article>
            <div className="safety-icon">
              <Flag aria-hidden="true" />
            </div>

            <h2>Report harmful behavior</h2>

            <p>
              Users can report inappropriate behavior
              and immediately leave a conversation.
            </p>
          </article>

          <article>
            <div className="safety-icon">
              <Users aria-hidden="true" />
            </div>

            <h2>Be respectful</h2>

            <p>
              Treat every person with respect and follow
              the community guidelines.
            </p>
          </article>
        </div>
      </section>
    </main>
  );
}

export default SafetyPage;