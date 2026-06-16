"""The backend abstraction.

Both the cloud and the local backend implement the same tiny interface, so the
rest of the service (routes, prompt building) is completely independent of which
provider is active. Selecting a provider is a configuration concern, handled by
``factory.create_backend``.
"""

from abc import ABC, abstractmethod


class Backend(ABC):
    @property
    @abstractmethod
    def name(self) -> str:
        """Stable identifier of the backend family: 'openai' or 'local'."""

    @property
    @abstractmethod
    def model(self) -> str:
        """The concrete model name being used."""

    @abstractmethod
    def generate(self, prompt: str, max_tokens: int) -> str:
        """Run a single text completion for the given prompt."""
