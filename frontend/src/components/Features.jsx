import {
  Zap,
  Video,
  ShieldCheck,
} from "lucide-react";

function Features() {
  return (
    <section className="features">
      <article className="feature-card">
        <div className="feature-icon">
          <Zap aria-hidden="true" />
        </div>

        <h3>Instant Matching</h3>
        <p>Connect with another person in seconds.</p>
      </article>

      <article className="feature-card">
        <div className="feature-icon">
          <Video aria-hidden="true" />
        </div>

        <h3>Video and Text</h3>
        <p>Choose how you want to communicate.</p>
      </article>

      <article className="feature-card">
        <div className="feature-icon">
          <ShieldCheck aria-hidden="true" />
        </div>

        <h3>Safety First</h3>
        <p>Built with moderation and reporting tools.</p>
      </article>
    </section>
  );
}

export default Features;